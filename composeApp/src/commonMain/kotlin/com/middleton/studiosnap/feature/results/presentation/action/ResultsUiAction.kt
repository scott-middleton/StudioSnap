package com.middleton.studiosnap.feature.results.presentation.action

sealed interface ResultsUiAction {
    data class OnDownloadClicked(val generationId: String) : ResultsUiAction
    data class OnShareClicked(val generationId: String) : ResultsUiAction
    data object OnDownloadAllClicked : ResultsUiAction
    data object OnBackClicked : ResultsUiAction
    data object OnDoneClicked : ResultsUiAction
    data object OnBuyCreditsClicked : ResultsUiAction
    data object OnSnackbarDismissed : ResultsUiAction
}
