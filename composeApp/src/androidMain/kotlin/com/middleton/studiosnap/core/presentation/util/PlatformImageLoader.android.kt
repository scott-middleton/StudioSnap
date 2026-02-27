package com.middleton.studiosnap.core.presentation.util

import android.util.Log
import androidx.core.net.toUri
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

private const val TAG = "PlatformImageLoader"

actual suspend fun loadImageBytesFromIdentifier(identifier: String): ByteArray? =
    withContext(Dispatchers.IO) {
        try {
            if (identifier.startsWith("content://")) {
                val context = AndroidContextHolder.context ?: return@withContext null
                val uri = identifier.toUri()
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else {
                val file = File(identifier)
                if (file.exists()) {
                    FileInputStream(file).use { it.readBytes() }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load image: $identifier", e)
            null
        }
    }
