package com.middleton.studiosnap.core.presentation.util

import android.content.Intent
import androidx.core.content.FileProvider
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun shareImage(filePath: String): Result<Unit> = withContext(Dispatchers.Main) {
    runCatching {
        val context = AndroidContextHolder.context
            ?: throw Exception("Android context not available")

        val uri = if (filePath.startsWith("content://")) {
            // Already a content URI (e.g. from gallery/MediaStore)
            android.net.Uri.parse(filePath)
        } else {
            // Local file path — use FileProvider
            val file = File(filePath)
            if (!file.exists()) {
                throw Exception("File not found")
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Image").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }
}
