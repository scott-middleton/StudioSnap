package com.middleton.studiosnap.feature.history.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
}

data class HistoryItem(
    val id: String,
    val previewUri: String,
    val styleName: UiText,
    val createdAt: Long
)
