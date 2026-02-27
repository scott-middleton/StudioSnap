package com.middleton.studiosnap.core.data.auth

import com.middleton.studiosnap.core.domain.exception.NotAuthenticatedException
import com.middleton.studiosnap.core.domain.model.AuthProvider
import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.service.AuthService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Supabase implementation of [AuthService]
 *
 * Uses Supabase Auth with native Apple/Google sign-in.
 * Session management is handled automatically by the Supabase SDK.
 *
 * Note: Actual sign-in UI is handled by Supabase ComposeAuth composables
 * (rememberSignInWithApple/Google). This service retrieves and manages the user session.
 */
class SupabaseAuthService(
    private val supabaseClient: SupabaseClient,
    coroutineScope: CoroutineScope
) : AuthService {

    private val _isSignedIn = MutableStateFlow(false)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private var initialized = false

    init {
        coroutineScope.launch {
            // Wait for Supabase to initialize before collecting session status
            supabaseClient.auth.awaitInitialization()
            // Set initial state based on current session
            _isSignedIn.value = supabaseClient.auth.currentSessionOrNull() != null
            initialized = true
            // Then collect future changes
            supabaseClient.auth.sessionStatus.collect { status ->
                _isSignedIn.value = status is SessionStatus.Authenticated
            }
        }
    }

    override suspend fun awaitInitialized(): Boolean {
        supabaseClient.auth.awaitInitialization()
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    override suspend fun signIn(): Result<AuthUser> {
        return getCurrentUser()?.let { Result.success(it) }
            ?: Result.failure(NotAuthenticatedException("No user session found"))
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Sign-out failed", e))
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull() ?: return null
            val user = session.user ?: return null

            AuthUser(
                id = user.id,
                email = user.email,
                displayName = extractDisplayName(user.userMetadata),
                provider = extractProvider(user.appMetadata)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDisplayName(userMetadata: Map<String, Any?>?): String? {
        if (userMetadata == null) return null

        return (userMetadata["full_name"] ?: userMetadata["name"])
            ?.toString()
            ?.trim('"')
            ?.takeIf { it.isNotBlank() }
    }

    private fun extractProvider(appMetadata: Map<String, Any?>?): AuthProvider {
        val providerString = appMetadata?.get("provider")
            ?.toString()
            ?.trim('"')
            ?.lowercase()

        return when (providerString) {
            "apple" -> AuthProvider.APPLE
            "google" -> AuthProvider.GOOGLE
            else -> AuthProvider.ANONYMOUS
        }
    }
}
