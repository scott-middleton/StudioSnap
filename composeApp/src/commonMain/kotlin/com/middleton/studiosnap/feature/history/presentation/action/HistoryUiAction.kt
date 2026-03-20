package com.middleton.studiosnap.feature.history.presentation.action

sealed interface HistoryUiAction {
    data class OnSessionClicked(val sessionId: String) : HistoryUiAction
    data class OnDeleteSessionClicked(val sessionId: String) : HistoryUiAction
    data object OnBackClicked : HistoryUiAction
    data object OnNavigationHandled : HistoryUiAction
}
