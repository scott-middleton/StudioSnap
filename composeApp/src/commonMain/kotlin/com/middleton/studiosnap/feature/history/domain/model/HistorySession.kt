package com.middleton.studiosnap.feature.history.domain.model

import com.middleton.studiosnap.core.domain.model.UiText

/**
 * A session is a single batch generation run — one style applied to N photos.
 * Legacy rows (pre-v3 DB) each appear as their own single-image session.
 */
data class HistorySession(
    val batchId: String,
    val thumbnailUris: List<String>,
    val imageCount: Int,
    val sessionLabel: String?,
    val styleDisplayName: UiText,
    val createdAt: Long
) {
    /** The display label: user-defined if set, otherwise the localized style name. */
    val displayLabel: UiText
        get() = sessionLabel?.let { UiText.DynamicString(it) } ?: styleDisplayName
}
