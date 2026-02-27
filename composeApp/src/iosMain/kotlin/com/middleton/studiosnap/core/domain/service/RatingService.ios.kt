package com.middleton.studiosnap.core.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

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
}
