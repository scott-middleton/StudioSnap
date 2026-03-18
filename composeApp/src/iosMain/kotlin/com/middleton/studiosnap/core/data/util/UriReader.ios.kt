package com.middleton.studiosnap.core.data.util

import com.middleton.studiosnap.core.presentation.imagepicker.IosImageCache
import com.middleton.studiosnap.core.presentation.util.toByteArray
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.UIKit.UIImageJPEGRepresentation

/**
 * Reads bytes from a URI on iOS.
 * Tries IosImageCache first (for photos just picked from the gallery),
 * then falls back to the file system (for generated images stored in app cache).
 */
actual fun readBytesFromUri(uri: String): ByteArray? {
    // Check in-memory cache first — holds UIImages from the last photo picker session
    val uiImage = IosImageCache.getImage(uri)
    if (uiImage != null) {
        val imageData = UIImageJPEGRepresentation(uiImage, 0.85) ?: return null
        return imageData.toByteArray()
    }

    // Fallback: treat URI as a file path (generated images stored in app cache dir)
    return try {
        val path = uri.toPath()
        if (FileSystem.SYSTEM.exists(path)) {
            FileSystem.SYSTEM.read(path) { readByteArray() }
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
