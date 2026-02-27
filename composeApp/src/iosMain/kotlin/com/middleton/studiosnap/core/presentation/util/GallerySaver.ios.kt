package com.middleton.studiosnap.core.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual suspend fun saveImageToGallery(filePath: String, fileName: String): Result<Unit> {
    return suspendCancellableCoroutine { continuation ->
        val imageData = NSData.dataWithContentsOfFile(filePath)
        if (imageData == null) {
            continuation.resume(Result.failure(Exception("Failed to read image file")))
            return@suspendCancellableCoroutine
        }

        val image = UIImage(data = imageData)

        val status = PHPhotoLibrary.authorizationStatus()
        if (status != PHAuthorizationStatusAuthorized) {
            PHPhotoLibrary.requestAuthorization { newStatus ->
                if (newStatus == PHAuthorizationStatusAuthorized) {
                    saveImageToPhotos(image, continuation)
                } else {
                    continuation.resume(Result.failure(Exception("Photo library access denied")))
                }
            }
        } else {
            saveImageToPhotos(image, continuation)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun saveImageToPhotos(
    image: UIImage,
    continuation: kotlinx.coroutines.CancellableContinuation<Result<Unit>>
) {
    UIImageWriteToSavedPhotosAlbum(
        image = image,
        completionTarget = null,
        completionSelector = null,
        contextInfo = null
    )
    // UIImageWriteToSavedPhotosAlbum doesn't have a proper callback in Kotlin/Native
    // We assume success if no exception was thrown
    continuation.resume(Result.success(Unit))
}
