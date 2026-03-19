package com.middleton.studiosnap.core.presentation.imagepicker

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePickerLauncher(
    maxSelection: Int,
    onImagesSelected: (List<ImagePickerResult>) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Always use unlimited multi-select and truncate client-side.
    // This avoids stale contract issues since rememberLauncherForActivityResult
    // doesn't recompose when the contract changes.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            try {
                val limitedUris = if (maxSelection > 0) uris.take(maxSelection) else uris
                val results = limitedUris.mapNotNull { uri ->
                    try {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream, null, options)
                        }

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

                        ImagePickerResult(
                            uri = uri.toString(),
                            width = options.outWidth.takeIf { it > 0 },
                            height = options.outHeight.takeIf { it > 0 },
                            fileName = fileName,
                            fileSize = fileSize,
                            mimeType = mimeType
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                if (results.isNotEmpty()) {
                    onImagesSelected(results)
                } else {
                    onError()
                }
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

// Android always returns null — PickedImage falls through to Coil (GalleryImage/SubcomposeAsyncImage)
// which handles content:// URIs with proper downsampling. Loading full-res bitmaps into
// ImageBitmap crashes with "Canvas: trying to draw too large bitmap" on high-res camera photos.
actual fun ImagePickerResult.loadImageBitmap(): ImageBitmap? = null

/**
 * Holder for Android context to be used in non-composable functions.
 * Must be initialized in Application or MainActivity.
 */
object AndroidContextHolder {
    var context: android.content.Context? = null
    var activity: android.app.Activity? = null
}
