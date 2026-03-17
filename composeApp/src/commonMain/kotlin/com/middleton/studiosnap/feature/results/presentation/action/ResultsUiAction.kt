package com.middleton.studiosnap.feature.results.presentation.action

sealed interface ResultsUiAction {
    data class OnSaveClicked(val generationId: String) : ResultsUiAction
    data class OnShareClicked(val generationId: String) : ResultsUiAction
    data class OnToggleBeforeAfter(val generationId: String) : ResultsUiAction
    data object OnSaveAllClicked : ResultsUiAction
    data object OnBackClicked : ResultsUiAction
    data object OnDoneClicked : ResultsUiAction
    data object OnSnackbarDismissed : ResultsUiAction
    data object OnNavigationHandled : ResultsUiAction
}
