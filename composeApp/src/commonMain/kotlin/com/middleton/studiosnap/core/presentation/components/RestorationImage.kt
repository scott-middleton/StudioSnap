package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

/**
 * Displays a restoration image, handling both gallery URIs and local file paths.
 * For watermarked (free trial) restorations stored as file paths, uses Coil directly.
 * For paid restorations stored as gallery URIs, delegates to platform-specific GalleryImage.
 *
 * @param imagePath The stored image path (gallery URI or file path)
 * @param isWatermarked Whether this is a watermarked (file-based) restoration
 * @param fallbackPath Optional fallback path if primary fails (e.g. originalImagePath)
 */
@Composable
fun RestorationImage(
    imagePath: String,
    isWatermarked: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    knownAspectRatio: Float? = null,
    fillContainer: Boolean = false,
    targetSizePx: Int? = null,
    fallbackPath: String? = null,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    if (isWatermarked) {
        // Watermarked images are stored as local file paths — use Coil directly
        SubcomposeAsyncImage(
            model = imagePath,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = { loading?.invoke() ?: Box(Modifier.fillMaxSize()) },
            error = {
                // Fallback to original image path if watermarked file is missing
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
    } else {
        // Paid images are in the gallery — use platform-specific loader
        GalleryImage(
            galleryUri = imagePath,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            knownAspectRatio = knownAspectRatio,
            fillContainer = fillContainer,
            targetSizePx = targetSizePx,
            loading = loading,
            error = error
        )
    }
}
