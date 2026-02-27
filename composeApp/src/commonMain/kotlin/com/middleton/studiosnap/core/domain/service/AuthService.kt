package com.middleton.studiosnap.core.domain.service

import com.middleton.studiosnap.core.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    /**
     * Observable auth state that emits true when user is signed in.
     * Updates reactively when session status changes (sign-in, sign-out, token refresh).
     */
    val isSignedIn: StateFlow<Boolean>

    /**
     * Waits for the auth system to initialize and returns the current sign-in state.
     * Use this at app startup to get the definitive auth state before navigation.
     */
    suspend fun awaitInitialized(): Boolean

    suspend fun signIn(): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): AuthUser?
}
