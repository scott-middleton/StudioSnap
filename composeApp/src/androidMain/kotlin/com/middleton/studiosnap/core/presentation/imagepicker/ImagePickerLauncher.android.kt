package com.middleton.studiosnap.core.presentation.imagepicker

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun ImagePickerLauncher(
    onImageSelected: (ImagePickerResult) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Get image dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }

                // Get file info
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                var fileName: String? = null
                var fileSize: Long? = null
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIndex >= 0) fileName = it.getString(nameIndex)
                        if (sizeIndex >= 0) fileSize = it.getLong(sizeIndex)
                    }
                }

                val mimeType = context.contentResolver.getType(uri)

                val result = ImagePickerResult(
                    uri = uri.toString(),
                    width = options.outWidth.takeIf { it > 0 },
                    height = options.outHeight.takeIf { it > 0 },
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType
                )

                onImageSelected(result)
            } catch (_: Exception) {
                onError()
            }
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

actual fun ImagePickerResult.loadImageBitmap(): ImageBitmap? {
    return try {
        val context = AndroidContextHolder.context ?: return null
        val uri = this.uri.toUri()
        context.decodeBitmapWithOrientation(uri)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

/**
 * Holder for Android context to be used in non-composable functions.
 * Must be initialized in Application or MainActivity.
 */
object AndroidContextHolder {
    var context: android.content.Context? = null
    var activity: android.app.Activity? = null
}
