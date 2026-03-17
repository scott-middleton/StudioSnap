package com.middleton.studiosnap.feature.history.presentation.action

sealed interface HistoryUiAction {
    data class OnItemClicked(val itemId: String) : HistoryUiAction
    data class OnDeleteClicked(val itemId: String) : HistoryUiAction
    data object OnBackClicked : HistoryUiAction
    data object OnNavigationHandled : HistoryUiAction
}
