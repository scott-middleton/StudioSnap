package com.middleton.studiosnap

import android.app.Application

/**
 * Custom Application class that initialises Koin before any Activity starts.
 *
 * This prevents the race condition in Koin's KoinApplication composable where
 * Android can trigger Compose recomposition during process restoration before
 * rememberKoinApplication() has had a chance to run, crashing with
 * "Koin context has not been initialized".
 */
class StudioSnapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
