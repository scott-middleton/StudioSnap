package com.middleton.studiosnap.feature.processing.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText

/**
 * Processing stages for a single unit in the generation pipeline.
 * Each (photo, style) unit cycles through all stages before the next unit starts.
 */
enum class ProcessingStatus {
    Preparing,
    Generating,
    Downloading
}

/** Which dimension the "X of N" counter counts: photos (N photos × 1 style) or styles (1 photo × N styles). */
enum class CounterMode {
    PHOTOS,
    STYLES
}

sealed interface ProcessingUiState {

    data object Loading : ProcessingUiState

    data class Processing(
        val currentUnitIndex: Int,
        val totalUnits: Int,
        val styleName: UiText,
        // Invariant: status must always reflect the current progress value.
        // When progress is non-null, status is derived from it by ProcessingViewModel.
        // When progress is null (between units), status resets to Preparing.
        val status: ProcessingStatus = ProcessingStatus.Preparing,
        val progress: Float? = null,
        val currentPhotoUri: String? = null,
        val counterMode: CounterMode = CounterMode.PHOTOS
    ) : ProcessingUiState {
        val overallProgress: Float
            get() {
                if (totalUnits == 0) return 0f
                val baseProgress = currentUnitIndex.toFloat() / totalUnits
                val perUnitWeight = 1f / totalUnits
                val intraUnitProgress = progress ?: when (status) {
                    ProcessingStatus.Preparing -> 0f
                    ProcessingStatus.Generating -> 0.3f
                    ProcessingStatus.Downloading -> 0.8f
                }
                return baseProgress + (perUnitWeight * intraUnitProgress)
            }
    }

    sealed interface Error : ProcessingUiState {
        data class Generic(val message: UiText) : Error
        data object InsufficientCredits : Error

        /**
         * Every (photo, style) unit in the batch failed — nothing to show on Results,
         * so the Processing screen stays put and offers Retry / Go Back instead.
         * Retry re-runs the whole batch from scratch (all failures were refunded).
         */
        data class AllFailed(val refundedCredits: Int) : Error
    }

    data object Complete : ProcessingUiState
}
