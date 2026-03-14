package com.middleton.studiosnap.feature.history.presentation.ui_state

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
}

data class HistoryItem(
    val id: String,
    val inputPhotoUri: String,
    val previewUri: String,
    val fullResLocalUri: String?,
    val styleName: String,
    val isPurchased: Boolean,
    val createdAt: Long,
    val imageWidth: Int,
    val imageHeight: Int
)

enum class HistoryFilter {
    ALL, PURCHASED, PREVIEWS
}
