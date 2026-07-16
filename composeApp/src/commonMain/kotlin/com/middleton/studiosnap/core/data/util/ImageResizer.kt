package com.middleton.studiosnap.core.data.util

import kotlin.math.abs

/**
 * Resizes image ByteArray to fit within maximum dimensions while maintaining aspect ratio.
 * Platform-specific implementation required.
 */
expect suspend fun ByteArray.resizeImage(maxWidth: Int, maxHeight: Int, quality: Int = 85): ByteArray?

/**
 * The fixed set of ~1-megapixel resolutions Flux Kontext is trained on. The model snaps every
 * conditioning image to whichever of these is closest to the image's own aspect ratio before
 * generating (see `prepare_kontext` in black-forest-labs/flux), regardless of what size is
 * uploaded. Sending anything other than the bucket it will pick either wastes upload bandwidth
 * (too large) or forces the model to upscale a softer image (too small).
 */
private val KONTEXT_RESOLUTIONS = listOf(
    672 to 1568, 688 to 1504, 720 to 1456, 752 to 1392, 800 to 1328,
    832 to 1248, 880 to 1184, 944 to 1104, 1024 to 1024, 1104 to 944,
    1184 to 880, 1248 to 832, 1328 to 800, 1392 to 752, 1456 to 720,
    1504 to 688, 1568 to 672,
)

/**
 * Finds the Kontext resolution bucket matching [width]x[height]'s aspect ratio, replicating the
 * model's own `min(abs(aspect_ratio - w / h), ...)` selection so our upload matches what it will
 * actually use.
 */
fun closestKontextResolution(width: Int, height: Int): Pair<Int, Int> {
    val aspectRatio = width.toFloat() / height.toFloat()
    return KONTEXT_RESOLUTIONS.minBy { (w, h) -> abs(aspectRatio - w.toFloat() / h.toFloat()) }
}

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
