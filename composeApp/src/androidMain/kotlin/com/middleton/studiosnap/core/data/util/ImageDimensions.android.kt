package com.middleton.studiosnap.core.data.util

import android.graphics.BitmapFactory

actual fun decodeImageDimensions(bytes: ByteArray): Pair<Int, Int>? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    val width = options.outWidth
    val height = options.outHeight
    return if (width > 0 && height > 0) Pair(width, height) else null
}
