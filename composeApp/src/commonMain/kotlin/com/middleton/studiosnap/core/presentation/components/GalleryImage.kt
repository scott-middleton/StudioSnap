package com.middleton.studiosnap.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Loads and displays an image from the device gallery.
 * Android: uses Coil with content:// URI
 * iOS: loads from PHAsset via localIdentifier
 */
/**
 * @param targetSizePx Max pixel dimension for the loaded image. Null = full resolution.
 *                     Use ~600 for grid thumbnails, null for detail/fullscreen.
 */
@Composable
expect fun GalleryImage(
    galleryUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    knownAspectRatio: Float? = null,
    fillContainer: Boolean = false,
    targetSizePx: Int? = null,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
)
