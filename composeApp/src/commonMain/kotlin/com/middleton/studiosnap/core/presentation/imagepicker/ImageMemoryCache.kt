package com.middleton.studiosnap.core.presentation.imagepicker

/**
 * Clears any in-memory image caches.
 * On iOS, this clears the IosImageCache that holds UIImage references.
 * On Android, this is a no-op as we don't hold images in memory.
 */
expect fun clearImageMemoryCache()