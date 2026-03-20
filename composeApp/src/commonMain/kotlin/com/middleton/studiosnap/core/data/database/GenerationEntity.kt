package com.middleton.studiosnap.core.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generations")
data class GenerationEntity(
    @PrimaryKey val id: String,
    val inputPhotoUri: String,
    val styleId: String,
    val styleName: String,
    val previewUri: String,
    val fullResUrl: String? = null,
    val fullResLocalUri: String? = null,
    val isPurchased: Boolean = false,
    val shadow: Boolean = false,
    val reflection: Boolean = false,
    val exportFormat: String,
    val createdAt: Long,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    // batchId groups all results from one generation session.
    // Legacy rows (pre-v3) have batchId = "" and are each treated as their own session.
    @ColumnInfo(defaultValue = "") val batchId: String = "",
    // User-defined label for the session. Null means show styleName as the label.
    val sessionLabel: String? = null
)
