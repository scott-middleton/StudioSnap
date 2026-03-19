package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerResult
import com.middleton.studiosnap.core.presentation.imagepicker.loadImageBitmap

/**
 * Displays an image from an ImagePickerResult, handling platform differences.
 * iOS: loads from IosImageCache via loadImageBitmap() — bitmap is already downsampled.
 * Android: loadImageBitmap() returns null, falls through to GalleryImage (Coil),
 *          which downsamples content:// URIs to display size — avoids OOM on camera photos.
 */
@Composable
fun PickedImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    val bitmap = remember(imageUri) {
        ImagePickerResult(uri = imageUri).loadImageBitmap()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        GalleryImage(
            galleryUri = imageUri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            fillContainer = true,
            loading = loading,
            error = error
        )
    }
}
