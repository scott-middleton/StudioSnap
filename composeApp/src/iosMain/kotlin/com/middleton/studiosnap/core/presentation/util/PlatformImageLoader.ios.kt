package com.middleton.studiosnap.core.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Photos.PHAsset
import platform.Photos.PHFetchOptions
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHImageRequestOptionsVersionCurrent
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadImageBytesFromIdentifier(identifier: String): ByteArray? {
    val bytesFromAsset = loadFromPHAsset(identifier)
    if (bytesFromAsset != null) return bytesFromAsset

    return loadFromFilePath(identifier)
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun loadFromPHAsset(localIdentifier: String): ByteArray? {
    return suspendCancellableCoroutine { continuation ->
        val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
            listOf(localIdentifier),
            PHFetchOptions()
        )

        val asset = fetchResult.firstObject as? PHAsset
        if (asset == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val options = PHImageRequestOptions().apply {
            synchronous = false
            deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
            version = PHImageRequestOptionsVersionCurrent
            networkAccessAllowed = true
        }

        PHImageManager.defaultManager().requestImageDataForAsset(
            asset,
            options = options
        ) { imageData, _, _, _ ->
            val bytes = imageData?.toByteArray()
            continuation.resume(bytes)
        }
    }
}

private fun loadFromFilePath(path: String): ByteArray? {
    val data = NSData.dataWithContentsOfFile(path) ?: return null
    return data.toByteArray()
}
