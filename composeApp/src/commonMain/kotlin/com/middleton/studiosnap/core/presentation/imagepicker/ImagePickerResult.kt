package com.middleton.studiosnap.core.presentation.imagepicker

/**
 * Platform-agnostic representation of a selected image.
 */
data class ImagePickerResult(
    val uri: String,
    val width: Int? = null,
    val height: Int? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val testBytes: ByteArray? = null
)
