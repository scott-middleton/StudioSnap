package com.middleton.studiosnap.core.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Photos.PHAsset
import platform.Photos.PHFetchOptions
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHImageRequestOptionsVersionCurrent
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIWindowScene
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual suspend fun shareImage(filePath: String): Result<Unit> = withContext(Dispatchers.Main) {
    runCatching {
        val image = loadImage(filePath)
            ?: throw Exception("Failed to load image for sharing")

        val activityViewController = UIActivityViewController(
            activityItems = listOf(image),
            applicationActivities = null
        )

        val rootViewController = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()
            ?.keyWindow
            ?.rootViewController
            ?: throw Exception("No root view controller found")

        rootViewController.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}

private suspend fun loadImage(filePath: String): UIImage? {
    // Try as local file first
    if (filePath.startsWith("/")) {
        val imageData = NSData.dataWithContentsOfFile(filePath)
        if (imageData != null) return UIImage(data = imageData)
    }

    // Try as PHAsset local identifier
    return loadImageFromPHAsset(filePath)
}

private suspend fun loadImageFromPHAsset(localIdentifier: String): UIImage? {
    val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
        listOf(localIdentifier),
        PHFetchOptions()
    )
    val asset = fetchResult.firstObject as? PHAsset ?: return null

    return suspendCoroutine { continuation ->
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
            val image = imageData?.let { UIImage(data = it) }
            continuation.resume(image)
        }
    }
}
