package com.middleton.studiosnap.core.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

actual suspend fun applyWatermark(imagePath: String, outputPath: String): Result<Unit> =
    withContext(Dispatchers.Default) {
        runCatching {
            val original = BitmapFactory.decodeFile(imagePath)
                ?: throw Exception("Cannot decode image at $imagePath")
            val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
            if (mutable != original) original.recycle()
            val canvas = Canvas(mutable)

            val w = mutable.width.toFloat()
            val h = mutable.height.toFloat()
            val textSize = min(w, h) * WATERMARK_TEXT_SIZE_RATIO.toFloat()

            val shadowPaint = Paint().apply {
                color = Color.argb(
                    (WATERMARK_SHADOW_ALPHA * 255).toInt(),
                    0, 0, 0
                )
                this.textSize = textSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.argb(
                    (WATERMARK_TEXT_ALPHA * 255).toInt(),
                    (WATERMARK_BRAND_BLUE_R * 255).toInt(),
                    (WATERMARK_BRAND_BLUE_G * 255).toInt(),
                    (WATERMARK_BRAND_BLUE_B * 255).toInt()
                )
                this.textSize = textSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textWidth = textPaint.measureText(WATERMARK_APP_NAME)
            val spacingX = textWidth * 2.0f
            val spacingY = textSize * 4.0f

            val angleRad = Math.toRadians(WATERMARK_ROTATION_DEGREES.toDouble())
            val diagonal = Math.sqrt((w * w + h * h).toDouble()).toFloat()

            canvas.save()
            canvas.rotate(WATERMARK_ROTATION_DEGREES.toFloat(), w / 2f, h / 2f)

            val startX = -diagonal
            val startY = -diagonal
            val endX = w + diagonal
            val endY = h + diagonal

            var y = startY
            while (y < endY) {
                var x = startX
                while (x < endX) {
                    canvas.drawText(WATERMARK_APP_NAME, x + 2f, y + 2f, shadowPaint)
                    canvas.drawText(WATERMARK_APP_NAME, x, y, textPaint)
                    x += spacingX
                }
                y += spacingY
            }

            canvas.restore()

            FileOutputStream(outputPath).use { out ->
                mutable.compress(Bitmap.CompressFormat.JPEG, (WATERMARK_JPEG_QUALITY * 100).toInt(), out)
            }
            mutable.recycle()
        }
    }
