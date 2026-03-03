package com.middleton.studiosnap.core.presentation.imagepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.middleton.studiosnap.core.presentation.util.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import org.jetbrains.skia.Image
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.concurrent.AtomicInt

@Composable
actual fun ImagePickerLauncher(
    maxSelection: Int,
    onImagesSelected: (List<ImagePickerResult>) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
) {
    val delegate = remember {
        PHPickerDelegate(
            onImagesSelected = onImagesSelected,
            onError = onError,
            onDismiss = onDismiss
        )
    }

    DisposableEffect(Unit) {
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = maxSelection.toLong()
        }

        val picker = PHPickerViewController(configuration = configuration).apply {
            setDelegate(delegate)
        }

        getRootViewController()?.presentViewController(picker, animated = true, completion = null)

        onDispose { }
    }
}

private fun getRootViewController() = UIApplication.sharedApplication.connectedScenes
    .filterIsInstance<UIWindowScene>()
    .firstOrNull()
    ?.keyWindow
    ?.rootViewController

private class PHPickerDelegate(
    private val onImagesSelected: (List<ImagePickerResult>) -> Unit,
    private val onError: () -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onDismiss()
            return
        }

        val imageResults = mutableListOf<ImagePickerResult>()
        val remaining = AtomicInt(results.size)
        var hasError = false

        results.forEach { result ->
            val itemProvider = result.itemProvider

            if (itemProvider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier)) {
                itemProvider.loadDataRepresentationForTypeIdentifier(
                    typeIdentifier = UTTypeImage.identifier
                ) { data, error ->
                    if (error != null || data == null) {
                        hasError = true
                    } else {
                        val uiImage = UIImage(data = data)
                        val imageUri = result.assetIdentifier ?: "selected_image_${currentTimeMillis()}"

                        val (imgWidth, imgHeight) = uiImage.size.useContents {
                            Pair(width.toInt(), height.toInt())
                        }

                        val imageResult = ImagePickerResult(
                            uri = imageUri,
                            width = imgWidth,
                            height = imgHeight,
                            fileName = null,
                            fileSize = null,
                            mimeType = "image/jpeg"
                        )

                        val normalizedImage = normalizeOrientation(uiImage)
                        IosImageCache.cacheImage(imageUri, normalizedImage)

                        synchronized(imageResults) {
                            imageResults.add(imageResult)
                        }
                    }

                    if (remaining.decrementAndGet() == 0) {
                        dispatch_async(dispatch_get_main_queue()) {
                            if (imageResults.isNotEmpty()) {
                                onImagesSelected(imageResults.toList())
                            } else if (hasError) {
                                onError()
                            } else {
                                onDismiss()
                            }
                        }
                    }
                }
            } else {
                if (remaining.decrementAndGet() == 0) {
                    dispatch_async(dispatch_get_main_queue()) {
                        if (imageResults.isNotEmpty()) {
                            onImagesSelected(imageResults.toList())
                        } else {
                            onError()
                        }
                    }
                }
            }
        }
    }
}

private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

@OptIn(ExperimentalForeignApi::class)
private fun normalizeOrientation(image: UIImage): UIImage {
    if (image.imageOrientation == UIImageOrientation.UIImageOrientationUp) return image

    val size = image.size
    UIGraphicsBeginImageContextWithOptions(size, false, image.scale)
    val (w, h) = size.useContents { width to height }
    image.drawInRect(CGRectMake(0.0, 0.0, w, h))
    val normalizedImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return normalizedImage ?: image
}

internal object IosImageCache {
    private val cache = mutableMapOf<String, UIImage>()

    fun cacheImage(uri: String, image: UIImage) {
        cache[uri] = image
    }

    fun getImage(uri: String): UIImage? = cache[uri]

    fun clear() {
        cache.clear()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun ImagePickerResult.loadImageBitmap(): ImageBitmap? {
    val uiImage = IosImageCache.getImage(this.uri) ?: return null
    val imageData = UIImageJPEGRepresentation(uiImage, 0.9) ?: return null

    return try {
        Image.makeFromEncoded(imageData.toByteArray()).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}
