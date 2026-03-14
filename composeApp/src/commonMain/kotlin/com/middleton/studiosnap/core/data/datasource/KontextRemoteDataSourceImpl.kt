package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.core.data.cache.ImageCacheManager
import com.middleton.studiosnap.core.data.model.KontextPredictionRequest
import com.middleton.studiosnap.core.data.model.ReplicatePredictionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.datetime.Clock
import kotlinx.io.readByteArray
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KontextRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val imageCacheManager: ImageCacheManager,
    private val cloudFunctions: CloudFunctionDataSource
) : KontextRemoteDataSource {

    companion object {
        private const val BUFFER_SIZE = 8192L
    }

    override suspend fun createPrediction(
        request: KontextPredictionRequest
    ): Result<ReplicatePredictionResponse> = runCatching {
        val input = buildMap<String, Any?> {
            put("prompt", request.input.prompt)
            put("input_image", request.input.inputImage)
            put("output_format", request.input.outputFormat)
            put("aspect_ratio", request.input.aspectRatio)
            put("guidance", request.input.guidance)
            put("num_inference_steps", request.input.numInferenceSteps)
        }

        val responseMap = cloudFunctions.createVersionPrediction(request.version, input)
        mapToReplicateResponse(responseMap)
    }

    override suspend fun getPrediction(
        predictionId: String
    ): Result<ReplicatePredictionResponse> = runCatching {
        val responseMap = cloudFunctions.getPrediction(predictionId)
        mapToReplicateResponse(responseMap)
    }

    override suspend fun downloadImage(url: String): Result<ByteArray> = runCatching {
        val response = httpClient.get(url)
        response.body()
    }

    override suspend fun downloadImageToFile(url: String): Result<String> = runCatching {
        val timestamp = Clock.System.now().epochSeconds
        val fileName = "generated_$timestamp.jpg"

        httpClient.prepareGet(url).execute { response ->
            val channel: ByteReadChannel = response.body()

            imageCacheManager.saveImageStreamToCache(fileName) { sink ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(BUFFER_SIZE)
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        sink.write(bytes)
                    }
                }
            }
        }
    }

    private fun mapToReplicateResponse(map: Map<String, Any?>): ReplicatePredictionResponse {
        val id = map["id"] as? String
            ?: throw IllegalStateException("Missing 'id' in prediction response")
        val status = map["status"] as? String
            ?: throw IllegalStateException("Missing 'status' in prediction response")

        return ReplicatePredictionResponse(
            id = id,
            status = status,
            output = map["output"]?.toJsonElement(),
            error = map["error"] as? String,
            createdAt = map["created_at"] as? String,
            startedAt = map["started_at"] as? String,
            completedAt = map["completed_at"] as? String
        )
    }

    private fun Any?.toJsonElement(): JsonElement? {
        return when (this) {
            null -> JsonNull
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is List<*> -> JsonArray(this.map { it.toJsonElement() ?: JsonNull })
            is Map<*, *> -> JsonObject(
                this.entries.associate { (k, v) -> k.toString() to (v.toJsonElement() ?: JsonNull) }
            )
            else -> JsonPrimitive(this.toString())
        }
    }
}