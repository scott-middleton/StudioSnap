package com.middleton.studiosnap.core.domain.service

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "AndroidRatingService"
private const val PACKAGE_ID = "com.middleton.studiosnap"

class AndroidRatingService : RatingService {
    override suspend fun openStoreReviewPage() {
        val context = AndroidContextHolder.context ?: return
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_ID"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            // Play Store app not installed — fall back to browser
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_ID"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override suspend fun requestReview() {
        val activity = AndroidContextHolder.activity ?: run {
            Log.w(TAG, "requestReview: no activity available")
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "requestReview: activity is finishing or destroyed")
            return
        }

        try {
            val manager = ReviewManagerFactory.create(activity)

            val reviewInfo: ReviewInfo? = suspendCancellableCoroutine { continuation ->
                manager.requestReviewFlow().addOnCompleteListener { task ->
                    continuation.resume(if (task.isSuccessful) task.result else null)
                }
            }
            reviewInfo ?: return

            // Re-check activity state — time may have passed during requestReviewFlow
            if (activity.isFinishing || activity.isDestroyed) return

            suspendCancellableCoroutine<Unit> { continuation ->
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                    continuation.resume(Unit)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "requestReview failed", e)
        }
    }
}
