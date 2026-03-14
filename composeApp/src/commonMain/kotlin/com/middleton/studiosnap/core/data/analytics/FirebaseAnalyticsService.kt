package com.middleton.studiosnap.core.data.analytics

import com.middleton.studiosnap.core.domain.service.AnalyticsService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

class FirebaseAnalyticsService : AnalyticsService {
    private val analytics by lazy {
        try {
            Firebase.analytics
        } catch (e: Exception) {
            null
        }
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            analytics?.logEvent(name, params.ifEmpty { null })
        } catch (_: Exception) {
            // Silently fail — analytics should never crash the app
        }
    }
}
