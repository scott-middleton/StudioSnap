package com.middleton.studiosnap.core.presentation.imagepicker

/**
 * Android doesn't hold images in a memory cache - we use content URIs.
 * This is a no-op.
 */
actual fun clearImageMemoryCache() {
    // No-op on Android - we use content URIs, not in-memory cache
}
