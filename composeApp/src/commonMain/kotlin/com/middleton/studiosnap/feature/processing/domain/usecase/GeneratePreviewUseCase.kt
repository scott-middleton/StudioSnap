package com.middleton.studiosnap.feature.processing.domain.usecase

import dev.gitlive.firebase.functions.FirebaseFunctionsException
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationError
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository

/**
 * Generates a styled image for a single (photo, style) unit.
 * Results are saved to history, stamped with the batch's id so all units from one
 * generation run group into a single history session.
 */
class GeneratePreviewUseCase(
    private val generationRepository: GenerationRepository,
    private val historyRepository: HistoryRepository,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(
        unit: GenerationConfig.GenerationUnit,
        config: GenerationConfig,
        deductionKey: String? = null,
        onProgress: (suspend (Float) -> Unit)? = null
    ): GenerationResult {
        val photo = unit.photo
        val result = generationRepository.generateImage(
            photo = photo,
            prompt = unit.resolvedPrompt,
            style = unit.style,
            exportFormat = config.exportFormat,
            quality = config.quality,
            deductionKey = deductionKey,
            onProgress = onProgress
        )

        return result.fold(
            onSuccess = { success ->
                // GenerationRepositoryImpl returns batchId = "" — stamp the real batch id here
                // so GenerationDao.getSessions()'s COALESCE(NULLIF(batchId,''), id) groups all
                // units from this run into one session. Legacy rows (saved before this fix)
                // keep batchId = "" and still render as individual sessions.
                val stamped = success.copy(batchId = config.batchId)
                historyRepository.save(stamped)
                stamped
            },
            onFailure = { throwable ->
                println("StudioSnap GeneratePreviewUseCase failure: ${throwable::class.simpleName} - ${throwable.message}")
                println(throwable.stackTraceToString())
                errorReporter.recordException(throwable)
                GenerationResult.Failure(
                    inputPhoto = photo,
                    error = mapError(throwable)
                )
            }
        )
    }

    private fun mapError(throwable: Throwable): GenerationError {
        return when (throwable) {
            is FirebaseFunctionsException -> when (throwable.code.name) {
                "RESOURCE_EXHAUSTED", "resource-exhausted" -> GenerationError.API_ERROR
                "INVALID_ARGUMENT", "invalid-argument" -> GenerationError.CONTENT_FILTERED
                else -> GenerationError.API_ERROR
            }
            is kotlinx.coroutines.TimeoutCancellationException -> GenerationError.TIMEOUT
            else -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    message.contains("connect") || message.contains("network") ||
                    message.contains("timeout") -> GenerationError.NETWORK
                    else -> GenerationError.UNKNOWN
                }
            }
        }
    }
}
