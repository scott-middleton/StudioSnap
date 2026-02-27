package com.middleton.studiosnap.core.data.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun ByteArray.resizeImage(maxWidth: Int, maxHeight: Int, quality: Int): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            // Convert ByteArray to NSData
            val nsData = this@resizeImage.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = this@resizeImage.size.toULong()
                )
            }

            // Create UIImage from NSData
            val uiImage = UIImage(data = nsData) ?: return@withContext null
            val (originalWidth, originalHeight) = uiImage.size.useContents {
                width.toInt() to height.toInt()
            }

            // Check if resize is needed
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                return@withContext this@resizeImage
            }

            // Calculate scale factor
            val scaleWidth = maxWidth.toDouble() / originalWidth.toDouble()
            val scaleHeight = maxHeight.toDouble() / originalHeight.toDouble()
            val scale = min(scaleWidth, scaleHeight)

            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()

            // Resize using UIGraphics
            val newSize = CGSizeMake(newWidth.toDouble(), newHeight.toDouble())
            platform.UIKit.UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            uiImage.drawInRect(platform.CoreGraphics.CGRectMake(0.0, 0.0, newWidth.toDouble(), newHeight.toDouble()))
            val resizedImage = platform.UIKit.UIGraphicsGetImageFromCurrentImageContext()
            platform.UIKit.UIGraphicsEndImageContext()

            if (resizedImage == null) return@withContext null

            // Convert back to ByteArray as JPEG
            val jpegQuality = quality / 100.0
            val imageData = UIImageJPEGRepresentation(resizedImage, jpegQuality)
                ?: return@withContext null

            ByteArray(imageData.length.toInt()).apply {
                usePinned { pinned ->
                    platform.posix.memcpy(
                        pinned.addressOf(0),
                        imageData.bytes,
                        imageData.length
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun ByteArray.convertToJpeg(quality: Int): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val nsData = this@convertToJpeg.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = this@convertToJpeg.size.toULong()
                )
            }

            val uiImage = UIImage(data = nsData) ?: return@withContext null

            val jpegQuality = quality / 100.0
            val imageData = UIImageJPEGRepresentation(uiImage, jpegQuality)
                ?: return@withContext null

            ByteArray(imageData.length.toInt()).apply {
                usePinned { pinned ->
                    platform.posix.memcpy(
                        pinned.addressOf(0),
                        imageData.bytes,
                        imageData.length
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.getImageDimensions(): Pair<Int, Int>? {
    return try {
        val nsData = this.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = this.size.toULong()
            )
        }

        val uiImage = UIImage(data = nsData) ?: return null
        val (width, height) = uiImage.size.useContents {
            this.width.toInt() to this.height.toInt()
        }

        if (width > 0 && height > 0) {
            Pair(width, height)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
