package com.middleton.studiosnap.core.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.middleton.studiosnap.composeapp.BuildKonfig
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider

/**
 * Android implementation using Google Credential Manager.
 * Presents the Google Sign-In bottom sheet and returns a Firebase AuthCredential.
 *
 * Requires [AndroidContextHolder.activity] to be set (done in MainActivity).
 * Uses application [context] for CredentialManager creation.
 */
actual class NativeAuthProvider(
    private val context: Context
) {
    actual suspend fun getCredential(): AuthCredential {
        val activity = AndroidContextHolder.activity
            ?: throw Exception("No activity available for sign-in")

        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildKonfig.GOOGLE_SERVER_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(activity, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            return GoogleAuthProvider.credential(googleIdTokenCredential.idToken, null)
        } catch (e: GetCredentialCancellationException) {
            throw Exception("Sign-in cancelled by user", e)
        }
    }
}