package com.middleton.studiosnap.core.data.database

import androidx.room.ColumnInfo

/**
 * Room query result for the session-grouping query.
 * Not an @Entity — used only as a query return type.
 *
 * sessionId = COALESCE(NULLIF(batchId,''), id), so legacy rows (batchId='')
 * each appear as their own session keyed by their row id.
 */
data class SessionSummaryEntity(
    @ColumnInfo(name = "sessionId") val sessionId: String,
    @ColumnInfo(name = "imageCount") val imageCount: Int,
    @ColumnInfo(name = "sessionLabel") val sessionLabel: String?,
    @ColumnInfo(name = "styleId") val styleId: String,
    @ColumnInfo(name = "styleName") val styleName: String,
    /**
     * Number of distinct styles in the session. When > 1, the raw styleId/styleName above are
     * SQLite-arbitrary picks from the group and must NOT be displayed — the repository shows
     * an "N styles" label instead.
     */
    @ColumnInfo(name = "styleCount") val styleCount: Int,
    @ColumnInfo(name = "latestCreatedAt") val latestCreatedAt: Long,
    /**
     * Comma-separated preview URIs (up to 4, oldest first). Null if none.
     * previewUri is always a local file path — commas are safe as the GROUP_CONCAT delimiter.
     */
    @ColumnInfo(name = "thumbnailUris") val thumbnailUris: String?
)
