package com.middleton.studiosnap.core.data.auth

import com.middleton.studiosnap.core.domain.model.AuthProvider
import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.service.AuthService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Firebase implementation of [AuthService]
 *
 * Uses Firebase Auth via GitLive KMP SDK.
 * Session management is handled automatically by Firebase.
 *
 * Note: Actual sign-in UI is handled by platform-specific NativeAuthProvider
 * (Google Credential Manager on Android, ASAuthorization on iOS).
 * This service manages the authenticated session after sign-in.
 */
class FirebaseAuthService(
    coroutineScope: CoroutineScope
) : AuthService {

    private val auth = Firebase.auth
    private val _isSignedIn = MutableStateFlow(auth.currentUser != null)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    init {
        coroutineScope.launch {
            auth.authStateChanged.collect { user ->
                _isSignedIn.update { user != null }
            }
        }
    }

    override suspend fun awaitInitialized(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signIn(): Result<AuthUser> {
        return getCurrentUser()?.let { Result.success(it) }
            ?: Result.failure(Exception("No user session found"))
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Sign-out failed", e))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No user to delete"))
            user.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            // Firebase may require recent authentication for sensitive operations.
            // The error message from Firebase typically contains "CREDENTIAL_TOO_OLD_LOGIN_AGAIN"
            // or similar when re-auth is needed.
            val message = if (e.message?.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) == true ||
                e.message?.contains("requires-recent-login", ignoreCase = true) == true
            ) {
                "Please sign out and sign back in, then try deleting your account again"
            } else {
                "Account deletion failed"
            }
            Result.failure(Exception(message, e))
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        val user = auth.currentUser ?: return null
        return AuthUser(
            id = user.uid,
            email = user.email,
            displayName = user.displayName,
            provider = extractProvider(user.providerData)
        )
    }

    private fun extractProvider(providerData: List<dev.gitlive.firebase.auth.UserInfo>): AuthProvider {
        return when {
            providerData.any { it.providerId == "apple.com" } -> AuthProvider.APPLE
            providerData.any { it.providerId == "google.com" } -> AuthProvider.GOOGLE
            else -> AuthProvider.ANONYMOUS
        }
    }
}