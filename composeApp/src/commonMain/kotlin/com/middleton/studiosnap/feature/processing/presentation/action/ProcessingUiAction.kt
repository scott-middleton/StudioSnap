package com.middleton.studiosnap.feature.processing.presentation.action

sealed interface ProcessingUiAction {
    data object OnRetryClicked : ProcessingUiAction
}
