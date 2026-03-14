package com.middleton.studiosnap.core.data.service

/**
 * iOS App Check initialization is done in Swift (iOSApp.swift)
 * using FirebaseAppCheck with AppAttestProvider.
 * This is a no-op on the Kotlin side.
 */
actual fun initializeAppCheck() {
    // No-op — iOS App Check is initialized in Swift before Kotlin starts
}
