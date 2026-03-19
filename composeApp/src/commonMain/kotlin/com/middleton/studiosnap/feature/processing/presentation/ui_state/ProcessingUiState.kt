package com.middleton.studiosnap.feature.processing.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText

/**
 * Processing stages for a single photo in the generation pipeline.
 * Each photo cycles through all stages before the next photo starts.
 */
enum class ProcessingStatus {
    Preparing,
    Generating,
    Downloading
}

sealed interface ProcessingUiState {

    data object Loading : ProcessingUiState

    data class Processing(
        val currentPhotoIndex: Int,
        val totalPhotos: Int,
        val styleName: UiText,
        // Invariant: status must always reflect the current progress value.
        // When progress is non-null, status is derived from it by ProcessingViewModel.
        // When progress is null (between photos), status resets to Preparing.
        val status: ProcessingStatus = ProcessingStatus.Preparing,
        val progress: Float? = null,
        val currentPhotoUri: String? = null
    ) : ProcessingUiState {
        val overallProgress: Float
            get() {
                if (totalPhotos == 0) return 0f
                val baseProgress = currentPhotoIndex.toFloat() / totalPhotos
                val perPhotoWeight = 1f / totalPhotos
                val intraPhotoProgress = progress ?: when (status) {
                    ProcessingStatus.Preparing -> 0f
                    ProcessingStatus.Generating -> 0.3f
                    ProcessingStatus.Downloading -> 0.8f
                }
                return baseProgress + (perPhotoWeight * intraPhotoProgress)
            }
    }

    data class Error(val message: String) : ProcessingUiState

    data object Complete : ProcessingUiState
}
