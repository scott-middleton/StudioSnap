package com.middleton.studiosnap.core.presentation.imagepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-agnostic image picker launcher.
 * Each platform provides its own implementation.
 */
@Composable
expect fun ImagePickerLauncher(
    onImageSelected: (ImagePickerResult) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
)

/**
 * Load an ImageBitmap from an ImagePickerResult.
 * Each platform provides its own implementation.
 */
expect fun ImagePickerResult.loadImageBitmap(): ImageBitmap?
