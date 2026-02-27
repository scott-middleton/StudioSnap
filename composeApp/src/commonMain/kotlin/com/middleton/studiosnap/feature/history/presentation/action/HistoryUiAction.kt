package com.middleton.studiosnap.feature.history.presentation.action

import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryFilter

sealed interface HistoryUiAction {
    data class OnItemClicked(val itemId: String) : HistoryUiAction
    data class OnDeleteClicked(val itemId: String) : HistoryUiAction
    data class OnFilterChanged(val filter: HistoryFilter) : HistoryUiAction
    data object OnBackClicked : HistoryUiAction
}
