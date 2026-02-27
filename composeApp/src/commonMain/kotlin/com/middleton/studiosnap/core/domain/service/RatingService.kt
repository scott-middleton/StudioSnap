package com.middleton.studiosnap.core.domain.service

/**
 * Platform-native app rating prompt.
 * Uses Google Play In-App Review on Android, SKStoreReviewRequest on iOS.
 * The OS decides whether to actually show the dialog — not guaranteed.
 */
interface RatingService {
    suspend fun requestReview()
}
