package com.middleton.studiosnap.core.data.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGContextRestoreGState
import platform.CoreGraphics.CGContextSaveGState
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGContextConcatCTM
import platform.CoreGraphics.CGPointMake
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.drawAtPoint
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun applyWatermark(imagePath: String, outputPath: String): Result<Unit> =
    withContext(Dispatchers.Main) {
        runCatching {
            val imageData = NSData.dataWithContentsOfFile(imagePath)
                ?: error("Cannot read image at $imagePath")
            val uiImage = UIImage(data = imageData)
            val size = uiImage.size

            val width = size.useContents { width }
            val height = size.useContents { height }

            UIGraphicsBeginImageContextWithOptions(size, true, uiImage.scale)
            val context = UIGraphicsGetCurrentContext()
                ?: error("No graphics context")

            uiImage.drawAtPoint(CGPointMake(0.0, 0.0))

            val textSize = min(width, height) * WATERMARK_TEXT_SIZE_RATIO
            val estimatedTextWidth = textSize * WATERMARK_APP_NAME.length * 0.6
            val spacingX = estimatedTextWidth * 2.0
            val spacingY = textSize * 4.0

            val diagonal = sqrt(width * width + height * height)
            val angleRad = WATERMARK_ROTATION_DEGREES * PI / 180.0

            CGContextSaveGState(context)
            CGContextTranslateCTM(context, width / 2.0, height / 2.0)
            CGContextConcatCTM(context, CGAffineTransformMakeRotation(angleRad))
            CGContextTranslateCTM(context, -width / 2.0, -height / 2.0)

            val font = UIFont.boldSystemFontOfSize(textSize)

            val shadowAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to font,
                NSForegroundColorAttributeName to UIColor(
                    red = 0.0,
                    green = 0.0,
                    blue = 0.0,
                    alpha = WATERMARK_SHADOW_ALPHA
                )
            )

            val textAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to font,
                NSForegroundColorAttributeName to UIColor(
                    red = WATERMARK_BRAND_BLUE_R,
                    green = WATERMARK_BRAND_BLUE_G,
                    blue = WATERMARK_BRAND_BLUE_B,
                    alpha = WATERMARK_TEXT_ALPHA
                )
            )

            val startCoord = -diagonal
            val endX = width + diagonal
            val endY = height + diagonal

            var y = startCoord
            while (y < endY) {
                var x = startCoord
                while (x < endX) {
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    (WATERMARK_APP_NAME as NSString).drawAtPoint(
                        CGPointMake(x + 2.0, y + 2.0),
                        withAttributes = shadowAttrs
                    )
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    (WATERMARK_APP_NAME as NSString).drawAtPoint(
                        CGPointMake(x, y),
                        withAttributes = textAttrs
                    )
                    x += spacingX
                }
                y += spacingY
            }

            CGContextRestoreGState(context)

            val resultImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()

            val jpegData = UIImageJPEGRepresentation(resultImage!!, WATERMARK_JPEG_QUALITY)
                ?: error("Failed to create JPEG data")

            val bytes = ByteArray(jpegData.length.toInt()).apply {
                usePinned { pinned ->
                    platform.posix.memcpy(
                        pinned.addressOf(0),
                        jpegData.bytes,
                        jpegData.length
                    )
                }
            }
            FileSystem.SYSTEM.write(outputPath.toPath()) { write(bytes) }
            Unit
        }
    }
