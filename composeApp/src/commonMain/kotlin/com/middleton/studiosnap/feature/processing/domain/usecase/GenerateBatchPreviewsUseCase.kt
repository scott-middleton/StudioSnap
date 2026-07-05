package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * Progress carried across a retry so already-processed photos are never re-run.
 * [results] holds one entry per photo already attempted (successes and refunded
 * failures alike); [refundedCredits] is the running refund count so far.
 */
data class BatchResumeState(
    val results: List<GenerationResult> = emptyList(),
    val refundedCredits: Int = 0
) {
    companion object {
        val EMPTY = BatchResumeState()
    }
}

/**
 * Sequentially generates styled images for all photos in a batch.
 * Deducts 1 credit per photo with server-side pending deduction tracking.
 * If generation fails, requests a refund of the most recent pending deduction.
 * Emits progress after each image completes. Does NOT run in parallel —
 * Replicate has rate limits and sequential is simpler for error handling.
 *
 * A [resumeState] resumes a batch after a mid-batch failure + retry: photos
 * already present in [BatchResumeState.results] (successes and refunded
 * failures alike) are skipped entirely — never re-deducted, never re-run.
 */
open class GenerateBatchPreviewsUseCase(
    private val generatePreviewUseCase: GeneratePreviewUseCase,
    private val creditDeductor: CreditDeductor
) {

    /**
     * Returns a Flow that emits [BatchProgress] after each photo is processed.
     * Consumer can cancel the flow to abort remaining images.
     */
    open operator fun invoke(
        config: GenerationConfig,
        resumeState: BatchResumeState = BatchResumeState.EMPTY,
        onPhotoProgress: (suspend (photoIndex: Int, progress: Float) -> Unit)? = null
    ): Flow<BatchProgress> = flow {
        val photoCount = config.photos.size
        val results = resumeState.results.toMutableList()
        val baseIndex = results.size
        var refundedCredits = resumeState.refundedCredits

        config.photos.drop(baseIndex).forEachIndexed { offset, photo ->
            val index = baseIndex + offset
            val idempotencyKey = "${config.batchId}-${photo.id}-${Clock.System.now().toEpochMilliseconds()}"
            creditDeductor.deductGenerationCredit(idempotencyKey)
                .getOrElse { throw it }

            val result = generatePreviewUseCase(photo, config, deductionKey = idempotencyKey) { progress ->
                onPhotoProgress?.invoke(index, progress)
            }

            if (result is GenerationResult.Failure) {
                creditDeductor.refundGenerationCredit(idempotencyKey).onSuccess { refundedCredits++ }
            }

            results.add(result)

            emit(
                BatchProgress(
                    currentIndex = index,
                    totalCount = photoCount,
                    results = results.toList(),
                    currentResult = result,
                    refundedCredits = refundedCredits
                )
            )
        }
    }
}

object GenerationProgressStages {
    const val GENERATING_START = 0.30f  // Preparing ends / Generating begins
    const val DOWNLOADING_START = 0.80f // Generating ends / Downloading begins
    // Stages sum: 0.30 preparing + 0.50 generating + 0.20 downloading = 1.00
}

data class BatchProgress(
    val currentIndex: Int,
    val totalCount: Int,
    val results: List<GenerationResult>,
    val currentResult: GenerationResult,
    val refundedCredits: Int = 0
) {
    val isComplete: Boolean get() = currentIndex == totalCount - 1
    val successCount: Int get() = results.count { it is GenerationResult.Success }
    val failureCount: Int get() = results.count { it is GenerationResult.Failure }
}
