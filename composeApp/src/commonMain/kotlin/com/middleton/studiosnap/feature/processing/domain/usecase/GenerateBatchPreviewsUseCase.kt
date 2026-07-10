package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.FreeGenerationGate
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class FreeTrialAlreadyUsedException : Exception("Free trial has already been used")

/**
 * Sequentially generates styled images for every (photo, style) unit in a batch.
 * Deducts 1 credit per unit with server-side pending deduction tracking.
 * If generation fails, requests a refund of the most recent pending deduction.
 * Emits progress after each unit completes. Does NOT run in parallel —
 * Replicate has rate limits and sequential is simpler for error handling.
 */
open class GenerateBatchPreviewsUseCase(
    private val generatePreviewUseCase: GeneratePreviewUseCase,
    private val creditDeductor: CreditDeductor,
    private val freeGenerationGate: FreeGenerationGate,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Returns a Flow that emits [BatchProgress] after each unit is processed.
     * Consumer can cancel the flow to abort remaining units.
     */
    open operator fun invoke(
        config: GenerationConfig,
        onPhotoProgress: (suspend (unitIndex: Int, progress: Float) -> Unit)? = null
    ): Flow<BatchProgress> = flow {
        val totalCount = config.unitCount
        val results = mutableListOf<GenerationResult>()
        var refundedCredits = 0

        if (config.isFreeGeneration) {
            val claimed = freeGenerationGate.claimFreeGeneration()
            if (!claimed) {
                throw FreeTrialAlreadyUsedException()
            }
        }

        config.units.forEachIndexed { index, unit ->
            if (!config.isFreeGeneration) {
                val idempotencyKey =
                    "${config.batchId}-${unit.photo.id}-${unit.style.id}-${Clock.System.now().toEpochMilliseconds()}"
                creditDeductor.deductGenerationCredit(idempotencyKey)
                    .getOrElse { throw it }
            }

            val result = generatePreviewUseCase(unit.photo, unit.style, config) { progress ->
                onPhotoProgress?.invoke(index, progress)
            }

            if (result is GenerationResult.Failure && !config.isFreeGeneration) {
                creditDeductor.refundGenerationCredit()
                refundedCredits++
            }

            results.add(result)

            emit(
                BatchProgress(
                    currentIndex = index,
                    totalCount = totalCount,
                    results = results.toList(),
                    currentResult = result,
                    refundedCredits = refundedCredits
                )
            )
        }

        if (config.isFreeGeneration) {
            userPreferencesRepository.setHasUsedFreeGeneration()
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
