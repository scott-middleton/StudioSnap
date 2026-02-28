package com.middleton.studiosnap.feature.processing.presentation.ui_state

sealed interface ProcessingUiState {

    data object Loading : ProcessingUiState

    data class Processing(
        val currentPhotoIndex: Int,
        val totalPhotos: Int,
        val styleName: String,
        val overallProgress: Float
    ) : ProcessingUiState

    data class Error(val message: String) : ProcessingUiState

    data object Complete : ProcessingUiState
}
