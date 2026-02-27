package com.middleton.studiosnap.core.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class)
actual fun decodeImageDimensions(bytes: ByteArray): Pair<Int, Int>? {
    return try {
        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        val image = UIImage(data = nsData) ?: return null
        val (w, h) = image.size.useContents { Pair(width.toInt(), height.toInt()) }
        if (w > 0 && h > 0) Pair(w, h) else null
    } catch (_: Exception) {
        null
    }
}
