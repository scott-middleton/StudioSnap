package com.middleton.studiosnap.core.data.util

/**
 * Resizes image ByteArray to fit within maximum dimensions while maintaining aspect ratio.
 * Platform-specific implementation required.
 */
expect suspend fun ByteArray.resizeImage(maxWidth: Int, maxHeight: Int, quality: Int = 85): ByteArray?

/**
 * Gets the dimensions of an image from ByteArray.
 * Returns Pair(width, height) or null if invalid.
 */
expect fun ByteArray.getImageDimensions(): Pair<Int, Int>?

/**
 * Converts image bytes (any format including PNG) to JPEG at the specified quality.
 * Used to reduce file size of API responses that return PNG.
 */
expect suspend fun ByteArray.convertToJpeg(quality: Int = 85): ByteArray?
