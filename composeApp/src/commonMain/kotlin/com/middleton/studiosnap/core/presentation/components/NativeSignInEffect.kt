package com.middleton.studiosnap.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.middleton.studiosnap.PlatformType
import com.middleton.studiosnap.getPlatform
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
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
    val supabaseClient: SupabaseClient = koinInject()

    val onSignInResult: (NativeSignInResult) -> Unit = { result ->
        onResult(result is NativeSignInResult.Success)
    }

    val platformType = getPlatform().type
    val signInAction = if (platformType == PlatformType.IOS) {
        supabaseClient.composeAuth.rememberSignInWithApple(
            onResult = onSignInResult,
            fallback = { onSignInResult(NativeSignInResult.Error("Sign in unavailable")) }
        )
    } else {
        supabaseClient.composeAuth.rememberSignInWithGoogle(
            onResult = onSignInResult,
            fallback = { onSignInResult(NativeSignInResult.Error("Sign in unavailable")) }
        )
    }

    LaunchedEffect(showSignIn) {
        if (showSignIn) {
            signInAction.startFlow()
        }
    }
}
