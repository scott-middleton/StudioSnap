package com.middleton.studiosnap.core.data.util

import android.net.Uri
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder

/**
 * Reads bytes from a URI on Android.
 * Handles content:// URIs via ContentResolver and file:// URIs via direct file reading.
 */
actual fun readBytesFromUri(uri: String): ByteArray? {
    return try {
        val context = AndroidContextHolder.context ?: return null
        val parsedUri = Uri.parse(uri)
        context.contentResolver.openInputStream(parsedUri)?.use { stream ->
            stream.readBytes()
        }
    } catch (_: Exception) {
        null
    }
}
