package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.toUri

@Composable
actual fun GalleryImage(
    galleryUri: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    knownAspectRatio: Float?,
    fillContainer: Boolean, // Android handles crop natively via ContentScale — not needed here
    targetSizePx: Int?,
    loading: @Composable (() -> Unit)?,
    error: @Composable (() -> Unit)?
) {
    val model = if (targetSizePx != null) {
        ImageRequest.Builder(LocalPlatformContext.current)
            .data(galleryUri.toUri())
            .size(targetSizePx)
            .build()
    } else {
        galleryUri.toUri()
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = { loading?.invoke() ?: Box(Modifier.fillMaxSize()) },
        error = { error?.invoke() ?: Box(Modifier.fillMaxSize()) }
    )
}
