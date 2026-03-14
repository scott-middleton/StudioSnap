package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Sequentially generates styled images for all photos in a batch.
 * Emits progress after each image completes. Does NOT run in parallel —
 * Replicate has rate limits and sequential is simpler for error handling.
 */
open class GenerateBatchPreviewsUseCase(
    private val generatePreviewUseCase: GeneratePreviewUseCase
) {

    /**
     * Returns a Flow that emits [BatchProgress] after each photo is processed.
     * Consumer can cancel the flow to abort remaining images.
     */
    open operator fun invoke(config: GenerationConfig): Flow<BatchProgress> = flow {
        val results = mutableListOf<GenerationResult>()

        config.photos.forEachIndexed { index, photo ->
            val result = generatePreviewUseCase(photo, config)
            results.add(result)

            emit(
                BatchProgress(
                    currentIndex = index,
                    totalCount = config.photos.size,
                    results = results.toList(),
                    currentResult = result
                )
            )
        }
    }
}

data class BatchProgress(
    val currentIndex: Int,
    val totalCount: Int,
    val results: List<GenerationResult>,
    val currentResult: GenerationResult
) {
    val isComplete: Boolean get() = currentIndex == totalCount - 1
    val successCount: Int get() = results.count { it is GenerationResult.Success }
    val failureCount: Int get() = results.count { it is GenerationResult.Failure }
}
