package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

/**
 * Displays a generated image from a local file path using Coil.
 *
 * @param imagePath The local file path to the generated image
 * @param fallbackPath Optional fallback path if primary fails (e.g. originalImagePath)
 */
@Composable
fun RestorationImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackPath: String? = null,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    // All generated images are stored as local file paths — use Coil directly
    SubcomposeAsyncImage(
        model = imagePath,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = { loading?.invoke() ?: Box(Modifier.fillMaxSize()) },
        error = {
            // Fallback to original image path if file is missing
            if (fallbackPath != null) {
                SubcomposeAsyncImage(
                    model = fallbackPath,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    loading = { loading?.invoke() ?: Box(Modifier.fillMaxSize()) },
                    error = { error?.invoke() ?: Box(Modifier.fillMaxSize()) }
                )
            } else {
                error?.invoke() ?: Box(Modifier.fillMaxSize())
            }
        }
    )
}
