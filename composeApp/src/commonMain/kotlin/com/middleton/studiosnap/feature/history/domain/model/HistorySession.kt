package com.middleton.studiosnap.feature.history.domain.model

/**
 * A session is a single batch generation run — one style applied to N photos.
 * Legacy rows (pre-v3 DB) each appear as their own single-image session.
 */
data class HistorySession(
    val batchId: String,
    val thumbnailUris: List<String>,
    val imageCount: Int,
    val sessionLabel: String?,
    val styleName: String,
    val createdAt: Long
) {
    /** The display label: user-defined if set, otherwise style name. */
    val displayLabel: String get() = sessionLabel ?: styleName
}
