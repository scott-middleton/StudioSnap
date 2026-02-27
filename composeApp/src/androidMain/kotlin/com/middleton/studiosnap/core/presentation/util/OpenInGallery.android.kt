package com.middleton.studiosnap.core.presentation.util

import android.content.Intent
import android.net.Uri
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun openInGallery(galleryUri: String): Result<Unit> = withContext(Dispatchers.Main) {
    try {
        val context = AndroidContextHolder.context
            ?: return@withContext Result.failure(Exception("Context unavailable"))
        val uri = Uri.parse(galleryUri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
