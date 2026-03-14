package com.middleton.studiosnap.core.data.auth

import dev.gitlive.firebase.auth.AuthCredential

/**
 * Platform-specific authentication provider that launches native sign-in UI
 * and returns a Firebase AuthCredential.
 *
 * Android: Google Sign-In via Credential Manager
 * iOS: Apple Sign-In via ASAuthorizationController
 */
expect class NativeAuthProvider {
    /**
     * Launches the platform-native sign-in UI and returns a Firebase AuthCredential.
     *
     * @throws Exception if sign-in is cancelled or fails
     */
    suspend fun getCredential(): AuthCredential
}