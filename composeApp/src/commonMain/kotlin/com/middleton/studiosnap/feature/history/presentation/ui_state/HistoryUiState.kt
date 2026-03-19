package com.middleton.studiosnap.feature.history.presentation.ui_state

import com.middleton.studiosnap.feature.history.domain.model.HistoryItem

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
}
