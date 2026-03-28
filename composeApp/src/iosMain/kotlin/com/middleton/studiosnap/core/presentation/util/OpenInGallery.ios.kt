package com.middleton.studiosnap.core.presentation.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun openInGallery(galleryUri: String): Result<Unit> = withContext(Dispatchers.Main) {
    try {
        // On iOS, open the Photos app. Content URIs aren't directly openable,
        // so we open the Photos app itself as a best-effort approach.
        // canOpenURL requires LSApplicationQueriesSchemes in Info.plist to return true in
        // production builds, so we skip the check. photos-redirect:// is a system URL that
        // is always available on iOS — use the non-deprecated open(_:options:completionHandler:).
        val photosUrl = NSURL.URLWithString("photos-redirect://")
            ?: return@withContext Result.failure(Exception("Invalid Photos URL"))
        suspendCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(
                photosUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = { success ->
                    if (success) continuation.resume(Result.success(Unit))
                    else continuation.resume(Result.failure(Exception("Cannot open Photos app")))
                }
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
