package com.middleton.studiosnap.feature.history.domain.model

data class HistoryItem(
    val id: String,
    val previewUri: String,
    val styleName: String,
    val createdAt: Long
)
