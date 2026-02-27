package com.middleton.studiosnap.core.presentation.imagepicker

import android.graphics.Bitmap
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream

actual suspend fun ImagePickerResult.toByteArray(): ByteArray? {
    testBytes?.let { return it }
    return try {
        val context = AndroidContextHolder.context ?: return null
        val uri = this.uri.toUri()

        val bitmap = context.decodeBitmapWithOrientation(uri) ?: return null

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.toByteArray()
    } catch (_: Exception) {
        null
    }
}