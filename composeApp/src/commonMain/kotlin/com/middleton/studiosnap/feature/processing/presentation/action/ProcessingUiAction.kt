package com.middleton.studiosnap.feature.processing.presentation.action

sealed interface ProcessingUiAction {
    data object OnRetryClicked : ProcessingUiAction
    data object OnCancelClicked : ProcessingUiAction
    data object OnNavigationHandled : ProcessingUiAction
}
