package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.core.data.cache.ImageCacheManager
import com.middleton.studiosnap.core.data.database.GenerationDao
import com.middleton.studiosnap.feature.home.domain.error.PredictionFailedException
import com.middleton.studiosnap.feature.home.domain.error.PredictionTimeoutException
import com.middleton.studiosnap.core.data.datasource.KontextRemoteDataSource
import com.middleton.studiosnap.core.data.model.KontextInput
import com.middleton.studiosnap.core.data.model.KontextPredictionRequest
import com.middleton.studiosnap.core.data.model.ReplicatePredictionResponse
import com.middleton.studiosnap.core.data.util.getImageDimensions
import com.middleton.studiosnap.core.data.util.readBytesFromUri
import com.middleton.studiosnap.core.data.util.resizeImage
import com.middleton.studiosnap.core.data.util.toBase64DataUri
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.home.domain.usecase.BuildKontextPromptUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationProgressStages
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.json.jsonPrimitive

class GenerationRepositoryImpl(
    private val kontextDataSource: KontextRemoteDataSource,
    private val imageCacheManager: ImageCacheManager,
    private val buildKontextPromptUseCase: BuildKontextPromptUseCase,
    private val generationDao: GenerationDao
) : GenerationRepository {

    override suspend fun generateImage(
        photo: ProductPhoto,
        style: Style,
        shadow: Boolean,
        reflection: Boolean,
        exportFormat: ExportFormat,
        quality: GenerationQuality,
        onProgress: (suspend (Float) -> Unit)?
    ): Result<GenerationResult.Success> = runCatching {
        // 1. Read and compress the input image
        // readBytesFromUri handles content:// URIs on Android (via ContentResolver)
        // and IosImageCache / file paths on iOS
        val imageBytes = readBytesFromUri(photo.localUri)
            ?: throw IllegalStateException("Cannot read input image at ${photo.localUri}")

        val resizedBytes = imageBytes.resizeImage(
            maxWidth = MAX_INPUT_WIDTH,
            maxHeight = MAX_INPUT_HEIGHT
        ) ?: imageBytes

        val base64DataUri = resizedBytes.toBase64DataUri()
        onProgress?.invoke(STAGE_PREPARING_IMAGE_READY)

        // 2. Build the Kontext prompt
        val prompt = buildKontextPromptUseCase(style, shadow, reflection)

        // 3. Create the prediction
        val request = KontextPredictionRequest(
            version = KONTEXT_MODEL_VERSION,
            input = KontextInput(
                prompt = prompt,
                inputImage = base64DataUri,
                outputFormat = "jpg",
                aspectRatio = exportFormat.apiValue,
                numInferenceSteps = quality.inferenceSteps
            )
        )

        val createResponse = kontextDataSource.createPrediction(request).getOrThrow()
        onProgress?.invoke(STAGE_PREPARING_REQUEST_SENT)

        // 4. Poll for completion
        val completedResponse = pollForCompletion(createResponse.id, onProgress)

        // 5. Extract output URL
        val outputUrl = completedResponse.output?.jsonPrimitive?.content
            ?: throw IllegalStateException("No output URL in completed prediction")

        // 6. Download the generated image
        val filePath = kontextDataSource.downloadImageToFile(outputUrl) { downloadFraction ->
            onProgress?.invoke(STAGE_DOWNLOAD_START + downloadFraction * STAGE_DOWNLOAD_WIDTH)
        }.getOrThrow()

        // 7. Get image dimensions
        val downloadedBytes = imageCacheManager.readImageFromCache(filePath)
        val dimensions = downloadedBytes?.getImageDimensions()

        GenerationResult.Success(
            generationId = createResponse.id,
            inputPhoto = photo,
            previewUri = filePath,
            fullResUrl = outputUrl,
            fullResUri = null,
            style = style,
            createdAt = Clock.System.now().epochSeconds,
            imageWidth = dimensions?.first ?: 0,
            imageHeight = dimensions?.second ?: 0
        )
    }

    override suspend fun downloadFullRes(generationId: String): Result<String> = runCatching {
        // Look up the generation to get its full-res URL
        val entity = generationDao.getById(generationId)
            ?: throw IllegalStateException("Generation $generationId not found in history")

        val fullResUrl = entity.fullResUrl
            ?: throw IllegalStateException("No full-res URL stored for generation $generationId")

        // Download to local storage
        kontextDataSource.downloadImageToFile(fullResUrl).getOrThrow()
    }

    private suspend fun pollForCompletion(
        predictionId: String,
        onProgress: (suspend (Float) -> Unit)?
    ): ReplicatePredictionResponse {
        var attempts = 0
        while (attempts < MAX_POLL_ATTEMPTS) {
            delay(POLL_INTERVAL_MS)
            val response = kontextDataSource.getPrediction(predictionId).getOrThrow()

            when (response.status) {
                "succeeded" -> return response
                "failed", "canceled" -> {
                    val errorMsg = response.error ?: "Prediction ${response.status}"
                    throw PredictionFailedException(errorMsg)
                }
                else -> {
                    attempts++
                    val pollFraction = (attempts.toFloat() / MAX_POLL_ATTEMPTS).coerceAtMost(0.95f)
                    onProgress?.invoke(STAGE_GENERATING_START + pollFraction * STAGE_GENERATING_WIDTH)
                }
            }
        }
        throw PredictionTimeoutException("Prediction timed out after $MAX_POLL_ATTEMPTS attempts")
    }

    companion object {
        private const val MAX_INPUT_WIDTH = 1024
        private const val MAX_INPUT_HEIGHT = 1024
        private const val POLL_INTERVAL_MS = 2000L
        private const val MAX_POLL_ATTEMPTS = 60

        // Preparing sub-stage ticks (0.00 → 0.30)
        private const val STAGE_PREPARING_IMAGE_READY = 0.10f  // image read + resized
        private const val STAGE_PREPARING_REQUEST_SENT = 0.25f // prediction created, about to poll

        private val STAGE_GENERATING_START get() = GenerationProgressStages.GENERATING_START
        private const val STAGE_GENERATING_WIDTH = 0.50f  // 0.30 → 0.80
        private val STAGE_DOWNLOAD_START get() = GenerationProgressStages.DOWNLOADING_START
        private const val STAGE_DOWNLOAD_WIDTH = 0.20f    // 0.80 → 1.00

        /**
         * Flux Kontext [dev] model version hash.
         * Must be updated manually when Replicate publishes a new version.
         * Check: https://replicate.com/black-forest-labs/flux-kontext-dev/versions
         */
        const val KONTEXT_MODEL_VERSION =
            "85723d503c17da3f9fd9cecfb9987a8bf60ef747fd8f68a25d7636f88260eb59"
    }
}
