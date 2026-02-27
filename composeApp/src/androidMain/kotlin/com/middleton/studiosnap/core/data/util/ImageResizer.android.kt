package com.middleton.studiosnap.core.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

actual suspend fun ByteArray.resizeImage(maxWidth: Int, maxHeight: Int, quality: Int): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            // First decode with inJustDecodeBounds to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(this@resizeImage, 0, this@resizeImage.size, options)

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            // Calculate if we need to resize
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                // Image is already small enough, return as-is
                return@withContext this@resizeImage
            }

            // Calculate scale factor to fit within max dimensions
            val scaleWidth = maxWidth.toFloat() / originalWidth.toFloat()
            val scaleHeight = maxHeight.toFloat() / originalHeight.toFloat()
            val scale = min(scaleWidth, scaleHeight)

            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()

            // Now decode the actual bitmap with sample size for initial downsampling
            val sampleSize = calculateInSampleSize(originalWidth, originalHeight, newWidth, newHeight)
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeByteArray(this@resizeImage, 0, this@resizeImage.size, options)
                ?: return@withContext null

            // Scale to exact dimensions if needed
            val scaledBitmap = if (bitmap.width != newWidth || bitmap.height != newHeight) {
                bitmap.scale(newWidth, newHeight).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            scaledBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}

actual fun ByteArray.getImageDimensions(): Pair<Int, Int>? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(this, 0, this.size, options)

        if (options.outWidth > 0 && options.outHeight > 0) {
            Pair(options.outWidth, options.outHeight)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

actual suspend fun ByteArray.convertToJpeg(quality: Int): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(this@convertToJpeg, 0, this@convertToJpeg.size)
                ?: return@withContext null
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            bitmap.recycle()
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}

private fun calculateInSampleSize(
    originalWidth: Int,
    originalHeight: Int,
    targetWidth: Int,
    targetHeight: Int
): Int {
    var inSampleSize = 1

    if (originalHeight > targetHeight || originalWidth > targetWidth) {
        val halfHeight = originalHeight / 2
        val halfWidth = originalWidth / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= targetHeight &&
            (halfWidth / inSampleSize) >= targetWidth
        ) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}
