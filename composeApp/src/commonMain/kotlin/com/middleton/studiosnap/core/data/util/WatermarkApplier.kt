package com.middleton.studiosnap.core.data.util

expect suspend fun applyWatermark(imagePath: String, outputPath: String): Result<Unit>

internal const val WATERMARK_APP_NAME = "Restore AI"
internal const val WATERMARK_ROTATION_DEGREES = -30
internal const val WATERMARK_TEXT_SIZE_RATIO = 0.08
internal const val WATERMARK_BRAND_BLUE_R = 0.24
internal const val WATERMARK_BRAND_BLUE_G = 0.47
internal const val WATERMARK_BRAND_BLUE_B = 0.96
internal const val WATERMARK_TEXT_ALPHA = 0.35
internal const val WATERMARK_SHADOW_ALPHA = 0.15
internal const val WATERMARK_JPEG_QUALITY = 0.9
