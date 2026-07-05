package com.middleton.studiosnap.feature.results.domain.usecase

import com.middleton.studiosnap.core.domain.exception.InsufficientCreditsException
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import kotlinx.datetime.Clock

/**
 * Downloads the full-resolution version of a generated image.
 * Deducts 1 credit upfront, refunds on failure.
 * Updates history entry with the local full-res URI.
 */
class DownloadFullResUseCase(
    private val generationRepository: GenerationRepository,
    private val historyRepository: HistoryRepository,
    private val creditDeductor: CreditDeductor,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(generationId: String): Result<String> {
        val idempotencyKey = "download-$generationId-${Clock.System.now().toEpochMilliseconds()}"
        val deductResult = creditDeductor.deductGenerationCredit(idempotencyKey)
        if (deductResult.isFailure) {
            return Result.failure(InsufficientCreditsException())
        }

        return generationRepository.downloadFullRes(generationId)
            .onSuccess { localUri ->
                historyRepository.markAsPurchased(generationId, localUri)
            }
            .onFailure { throwable ->
                creditDeductor.refundGenerationCredit(idempotencyKey)
                    .onFailure { refundError -> errorReporter.recordException(refundError) }
                errorReporter.recordException(throwable)
            }
    }
}
