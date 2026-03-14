package com.middleton.studiosnap.core.data.repository

import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHFetchOptions
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Foundation.NSFileManager
import kotlin.coroutines.resume

class IosGalleryRepository : GalleryRepository {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveImage(filePath: String, displayName: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val imageData = NSData.dataWithContentsOfFile(filePath)
            if (imageData == null) {
                continuation.resume(Result.failure(Exception("Failed to read image file")))
                return@suspendCancellableCoroutine
            }

            val image = UIImage(data = imageData)

            ensureAuthorized { authorized ->
                if (!authorized) {
                    continuation.resume(Result.failure(Exception("Photo library access denied")))
                    return@ensureAuthorized
                }

                var localIdentifier: String? = null

                PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                    val request = PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                    localIdentifier = request.placeholderForCreatedAsset?.localIdentifier
                }) { success, error ->
                    if (success && localIdentifier != null) {
                        continuation.resume(Result.success(localIdentifier!!))
                    } else {
                        continuation.resume(
                            Result.failure(
                                Exception(error?.localizedDescription ?: "Failed to save image")
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun deleteImage(galleryUri: String): Result<Unit> {
        // File paths (generated image files) — delete directly via NSFileManager
        if (galleryUri.startsWith("/")) {
            return try {
                NSFileManager.defaultManager().removeItemAtPath(galleryUri, null)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return suspendCancellableCoroutine { continuation ->
            ensureAuthorized { authorized ->
                if (!authorized) {
                    continuation.resume(Result.failure(Exception("Photo library access denied")))
                    return@ensureAuthorized
                }

                val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                    listOf(galleryUri),
                    PHFetchOptions()
                )

                if (fetchResult.count.toInt() == 0) {
                    // Already deleted
                    continuation.resume(Result.success(Unit))
                    return@ensureAuthorized
                }

                PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                    PHAssetChangeRequest.deleteAssets(fetchResult)
                }) { success, error ->
                    if (success) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        continuation.resume(
                            Result.failure(
                                Exception(error?.localizedDescription ?: "Failed to delete image")
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun imageExists(galleryUri: String): Boolean {
        // File paths (generated image files) — check via NSFileManager
        if (galleryUri.startsWith("/")) {
            return NSFileManager.defaultManager().fileExistsAtPath(galleryUri)
        }

        return try {
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                listOf(galleryUri),
                PHFetchOptions()
            )
            fetchResult.count.toInt() > 0
        } catch (_: Exception) {
            false
        }
    }

    private fun ensureAuthorized(callback: (Boolean) -> Unit) {
        val status = PHPhotoLibrary.authorizationStatus()
        if (status == PHAuthorizationStatusAuthorized) {
            callback(true)
        } else {
            PHPhotoLibrary.requestAuthorization { newStatus ->
                callback(newStatus == PHAuthorizationStatusAuthorized)
            }
        }
    }
}
