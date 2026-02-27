package com.middleton.studiosnap.feature.results.domain.usecase

import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository

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
        // Deduct credit upfront
        val deductResult = creditDeductor.deductCredits(DOWNLOAD_CREDIT_COST, "full_res_download")
        if (deductResult.isFailure) {
            return Result.failure(InsufficientCreditsException())
        }

        return generationRepository.downloadFullRes(generationId)
            .onSuccess { localUri ->
                historyRepository.markAsPurchased(generationId, localUri)
            }
            .onFailure { throwable ->
                // Refund on failure
                creditDeductor.refundCredits(DOWNLOAD_CREDIT_COST, "download_failed_refund")
                errorReporter.recordException(throwable)
            }
    }

    companion object {
        const val DOWNLOAD_CREDIT_COST = 1
    }
}

class InsufficientCreditsException : Exception("Not enough credits to download")
