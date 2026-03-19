package com.middleton.studiosnap.feature.history.domain.model

import com.middleton.studiosnap.core.domain.model.UiText

data class HistoryItem(
    val id: String,
    val previewUri: String,
    val styleName: UiText,
    val createdAt: Long
)
