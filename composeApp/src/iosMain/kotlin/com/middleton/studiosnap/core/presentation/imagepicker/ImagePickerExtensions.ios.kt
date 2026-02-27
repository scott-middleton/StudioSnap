package com.middleton.studiosnap.core.presentation.imagepicker

import com.middleton.studiosnap.core.presentation.util.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImageJPEGRepresentation

@OptIn(ExperimentalForeignApi::class)
actual suspend fun ImagePickerResult.toByteArray(): ByteArray? {
    testBytes?.let { return it }
    return try {
        val uiImage = IosImageCache.getImage(this.uri) ?: return null
        val imageData = UIImageJPEGRepresentation(uiImage, 0.85) ?: return null
        imageData.toByteArray()
    } catch (_: Exception) {
        null
    }
}