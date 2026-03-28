package com.middleton.studiosnap.core.domain.service

/**
 * Platform-native app rating/review handling.
 *
 * [requestReview] — OS-controlled in-app prompt (SKStoreReviewController / Play In-App Review).
 *   The OS decides whether the dialog is shown; not guaranteed. Use at natural moments in the
 *   user flow, never from a direct button tap.
 *
 * [openStoreReviewPage] — Opens the App Store / Play Store review page directly.
 *   Always works; no OS gate-keeping. Use for explicit "Rate App" actions in Settings.
 */
interface RatingService {
    suspend fun requestReview()
    suspend fun openStoreReviewPage()
}
