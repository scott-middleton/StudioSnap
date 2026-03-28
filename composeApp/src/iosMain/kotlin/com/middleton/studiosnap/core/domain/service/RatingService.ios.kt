package com.middleton.studiosnap.core.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val APP_STORE_REVIEW_URL =
    "https://apps.apple.com/app/id6761026965?action=write-review"

class IosRatingService : RatingService {
    override suspend fun requestReview() = withContext(Dispatchers.Main) {
        // requestReviewInScene is preferred (iOS 14+, non-deprecated).
        // Falls back to the class-level requestReview if no scene is found.
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()

        if (scene != null) {
            SKStoreReviewController.requestReviewInScene(scene)
        } else {
            @Suppress("DEPRECATION")
            SKStoreReviewController.requestReview()
        }
    }

    override suspend fun openStoreReviewPage() = withContext(Dispatchers.Main) {
        val url = NSURL.URLWithString(APP_STORE_REVIEW_URL) ?: return@withContext
        suspendCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(
                url,
                options = emptyMap<Any?, Any>(),
                completionHandler = { continuation.resume(Unit) }
            )
        }
    }
}
