package com.middleton.studiosnap.core.data.analytics

import com.middleton.studiosnap.core.domain.service.AnalyticsService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

class FirebaseAnalyticsService : AnalyticsService {
    private val analytics by lazy {
        try {
            Firebase.analytics.also {
                println("FirebaseAnalytics: initialized successfully")
            }
        } catch (e: Exception) {
            println("FirebaseAnalytics: init FAILED - ${e.message}")
            null
        }
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            val instance = analytics
            if (instance != null) {
                instance.logEvent(name, params.ifEmpty { null })
                println("FirebaseAnalytics: logged event '$name' with ${params.size} params")
            } else {
                println("FirebaseAnalytics: skipped event '$name' (not initialized)")
            }
        } catch (e: Exception) {
            println("FirebaseAnalytics: FAILED to log '$name' - ${e.message}")
        }
    }
}
