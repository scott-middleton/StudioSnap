package com.middleton.studiosnap.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.middleton.studiosnap.core.data.auth.NativeAuthProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.compose.koinInject

/**
 * Handles native sign-in flow (Google on Android, Apple on iOS).
 * Triggers sign-in when [showSignIn] becomes true and reports the result.
 *
 * @param showSignIn Whether to trigger the sign-in flow
 * @param onResult Called with true on success, false on failure/cancellation
 */
@Composable
fun NativeSignInEffect(
    showSignIn: Boolean,
    onResult: (success: Boolean) -> Unit
) {
    val nativeAuthProvider: NativeAuthProvider = koinInject()

    LaunchedEffect(showSignIn) {
        if (showSignIn) {
            try {
                val credential = nativeAuthProvider.getCredential()
                Firebase.auth.signInWithCredential(credential)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
