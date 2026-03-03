package com.middleton.studiosnap.core.presentation.imagepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-agnostic image picker launcher with multi-select support.
 * Each platform provides its own implementation using native multi-select UI.
 *
 * @param maxSelection Maximum number of images the user can select (0 = unlimited).
 * @param onImagesSelected Called with the list of selected images.
 * @param onError Called when an error occurs during image loading.
 * @param onDismiss Called when the user cancels without selecting.
 */
@Composable
expect fun ImagePickerLauncher(
    maxSelection: Int = 0,
    onImagesSelected: (List<ImagePickerResult>) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
)

/**
 * Load an ImageBitmap from an ImagePickerResult.
 * Each platform provides its own implementation.
 */
expect fun ImagePickerResult.loadImageBitmap(): ImageBitmap?
