package com.middleton.studiosnap.core.presentation.imagepicker

/**
 * Clears the iOS image cache that holds UIImage references.
 * Called after saving the original image to disk to free memory.
 */
actual fun clearImageMemoryCache() {
    IosImageCache.clear()
}
