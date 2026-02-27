package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import coil3.toUri
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerResult
import com.middleton.studiosnap.core.presentation.imagepicker.loadImageBitmap

/**
 * Displays an image from an ImagePickerResult, handling platform differences.
 * iOS: loads from IosImageCache via loadImageBitmap()
 * Android: loads via Coil from content:// URI
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
        SubcomposeAsyncImage(
            model = imageUri.toUri(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = { loading?.invoke() ?: Box(Modifier.fillMaxSize()) },
            error = { error?.invoke() ?: Box(Modifier.fillMaxSize()) }
        )
    }
}
