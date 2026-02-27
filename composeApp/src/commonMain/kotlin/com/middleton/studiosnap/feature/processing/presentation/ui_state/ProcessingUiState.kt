package com.middleton.studiosnap.feature.processing.presentation.ui_state

sealed interface ProcessingUiState {

    data object Loading : ProcessingUiState

    data class Processing(
        val currentPhotoIndex: Int,
        val totalPhotos: Int,
        val styleName: String,
        val overallProgress: Float
    ) : ProcessingUiState {
        val progressText: String
            get() = if (totalPhotos == 1) styleName
            else "Photo ${currentPhotoIndex + 1} of $totalPhotos"
    }

    data class Error(val message: String) : ProcessingUiState

    data object Complete : ProcessingUiState
}
