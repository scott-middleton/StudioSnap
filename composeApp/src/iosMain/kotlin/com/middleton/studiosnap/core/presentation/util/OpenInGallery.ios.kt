package com.middleton.studiosnap.core.presentation.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual suspend fun openInGallery(galleryUri: String): Result<Unit> = withContext(Dispatchers.Main) {
    try {
        // On iOS, open the Photos app. Content URIs aren't directly openable,
        // so we open the Photos app itself as a best-effort approach.
        val photosUrl = NSURL.URLWithString("photos-redirect://")
        if (photosUrl != null && UIApplication.sharedApplication.canOpenURL(photosUrl)) {
            UIApplication.sharedApplication.openURL(photosUrl)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Cannot open Photos app"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
