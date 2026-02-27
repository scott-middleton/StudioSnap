package com.middleton.studiosnap.core.data.analytics

import com.middleton.studiosnap.composeapp.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

object CrashlyticsService {
    private val crashlytics by lazy {
        try {
            Firebase.crashlytics
        } catch (_: Exception) { null }
    }

    fun recordException(exception: Throwable) {
        try { crashlytics?.recordException(exception) } catch (_: Exception) {}
    }

    fun log(message: String) {
        try { crashlytics?.log(message) } catch (_: Exception) {}
    }

    fun setUserId(userId: String) {
        try { crashlytics?.setUserId(userId) } catch (_: Exception) {}
    }
}
