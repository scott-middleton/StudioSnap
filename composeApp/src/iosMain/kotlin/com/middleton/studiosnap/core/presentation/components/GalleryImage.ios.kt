package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import platform.Photos.PHAsset
import platform.Photos.PHFetchOptions
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHImageRequestOptionsResizeModeExact
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import com.middleton.studiosnap.core.presentation.util.toByteArray
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHImageContentModeAspectFit
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGSize
import kotlin.coroutines.resume

/**
 * Simple LRU memory cache for gallery images to prevent re-fetching on scroll.
 */
private object GalleryImageCache {
    private const val MAX_SIZE = 50
    private val cache = mutableMapOf<String, ImageBitmap>()
    private val accessOrder = mutableListOf<String>()
    private val lock = platform.Foundation.NSLock()

    private inline fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    fun get(key: String): ImageBitmap? = withLock {
        val bitmap = cache[key]
        if (bitmap != null) {
            accessOrder.remove(key)
            accessOrder.add(key)
        }
        bitmap
    }

    fun put(key: String, bitmap: ImageBitmap) = withLock {
        if (cache.size >= MAX_SIZE && !cache.containsKey(key)) {
            val oldest = accessOrder.removeFirst()
            cache.remove(oldest)
        }
        cache[key] = bitmap
        accessOrder.remove(key)
        accessOrder.add(key)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GalleryImage(
    galleryUri: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    knownAspectRatio: Float?,
    fillContainer: Boolean,
    targetSizePx: Int?,
    loading: @Composable (() -> Unit)?,
    error: @Composable (() -> Unit)?
) {
    // Cache key includes size to avoid serving thumbnails for fullscreen
    val cacheKey = if (targetSizePx != null) "$galleryUri@${targetSizePx}" else galleryUri

    // Check memory cache first — avoids async reload on scroll
    var bitmap by remember(cacheKey) {
        mutableStateOf(GalleryImageCache.get(cacheKey))
    }
    var isLoading by remember(cacheKey) { mutableStateOf(bitmap == null) }
    var isError by remember(cacheKey) { mutableStateOf(false) }

    // Use stored dimensions if available, otherwise query PHAsset metadata
    val aspectRatio = remember(galleryUri, knownAspectRatio) {
        knownAspectRatio ?: run {
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                listOf(galleryUri),
                PHFetchOptions()
            )
            val asset = fetchResult.firstObject as? PHAsset
            if (asset != null && asset.pixelWidth > 0u && asset.pixelHeight > 0u) {
                asset.pixelWidth.toFloat() / asset.pixelHeight.toFloat()
            } else {
                null
            }
        }
    }

    if (bitmap == null) {
        LaunchedEffect(cacheKey) {
            val loaded = loadImageFromPhotoLibrary(galleryUri, targetSizePx)
            if (loaded != null) {
                GalleryImageCache.put(cacheKey, loaded)
            }
            bitmap = loaded
            isLoading = false
            isError = loaded == null
        }
    }

    // When fillContainer=true the parent already constrains the size (e.g. a fixed-height card),
    // so we must NOT apply aspectRatio — it would fight the parent and leave letterbox bars.
    // ContentScale.Crop on the Image itself handles filling + cropping correctly.
    val sizedModifier = if (!fillContainer && aspectRatio != null) {
        modifier.aspectRatio(aspectRatio)
    } else {
        modifier
    }

    val loadedBitmap = bitmap
    when {
        isLoading -> loading?.invoke() ?: Box(sizedModifier)
        loadedBitmap != null -> Image(
            bitmap = loadedBitmap,
            contentDescription = contentDescription,
            modifier = sizedModifier,
            contentScale = contentScale
        )
        isError -> error?.invoke() ?: Box(sizedModifier)
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun loadImageFromPhotoLibrary(localIdentifier: String, targetSizePx: Int? = null): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                listOf(localIdentifier),
                PHFetchOptions()
            )

            val asset = fetchResult.firstObject as? PHAsset
            if (asset == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            var hasResumed = false

            val options = PHImageRequestOptions().apply {
                synchronous = false
                deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
                resizeMode = PHImageRequestOptionsResizeModeExact
                networkAccessAllowed = true
            }

            PHImageManager.defaultManager().requestImageForAsset(
                asset = asset,
                targetSize = cValue<CGSize> {
                    val size = (targetSizePx ?: 2048).toDouble()
                    width = size; height = size
                },
                contentMode = PHImageContentModeAspectFit,
                options = options
            ) { uiImage, _ ->
                if (hasResumed) return@requestImageForAsset
                hasResumed = true

                if (uiImage == null) {
                    continuation.resume(null)
                    return@requestImageForAsset
                }

                try {
                    val data = UIImageJPEGRepresentation(uiImage, 0.85)
                    if (data == null) {
                        continuation.resume(null)
                        return@requestImageForAsset
                    }
                    val bytes = data.toByteArray()
                    val skiImage = SkiaImage.makeFromEncoded(bytes)
                    continuation.resume(skiImage.toComposeImageBitmap())
                } catch (_: Exception) {
                    continuation.resume(null)
                }
            }
        }
    }
}
