package com.middleton.studiosnap.core.data.util

/**
 * Decodes image dimensions from raw bytes without fully loading the image.
 * Returns (width, height) or null if decoding fails.
 */
expect fun decodeImageDimensions(bytes: ByteArray): Pair<Int, Int>?
