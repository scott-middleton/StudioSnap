package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationError
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository

/**
 * Generates a watermarked preview for a single photo.
 * Previews are free — no credits deducted. Results saved to history.
 */
class GeneratePreviewUseCase(
    private val generationRepository: GenerationRepository,
    private val historyRepository: HistoryRepository,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(
        photo: ProductPhoto,
        config: GenerationConfig
    ): GenerationResult {
        val result = generationRepository.generateImage(
            photo = photo,
            style = config.style,
            shadow = config.shadow,
            reflection = config.reflection,
            exportFormat = config.exportFormat,
            quality = config.quality
        )

        return result.fold(
            onSuccess = { success ->
                historyRepository.save(success)
                success
            },
            onFailure = { throwable ->
                errorReporter.recordException(throwable)
                GenerationResult.Failure(
                    inputPhoto = photo,
                    error = mapError(throwable)
                )
            }
        )
    }

    private fun mapError(throwable: Throwable): GenerationError {
        return when {
            throwable.message?.contains("timeout", ignoreCase = true) == true ->
                GenerationError.TIMEOUT
            throwable.message?.contains("network", ignoreCase = true) == true ->
                GenerationError.NETWORK
            throwable.message?.contains("content", ignoreCase = true) == true ->
                GenerationError.CONTENT_FILTERED
            else -> GenerationError.UNKNOWN
        }
    }
}
