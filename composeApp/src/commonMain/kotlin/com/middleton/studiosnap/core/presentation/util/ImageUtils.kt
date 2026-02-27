package com.middleton.studiosnap.core.presentation.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Converts a ByteArray to an ImageBitmap.
 * Platform-specific implementation required.
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap?
