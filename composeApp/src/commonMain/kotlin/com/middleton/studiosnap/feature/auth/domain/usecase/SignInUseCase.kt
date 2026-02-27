package com.middleton.studiosnap.feature.auth.domain.usecase

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.purchases.PurchasesManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Use case for handling the post-sign-in flow
 * Called after successful native authentication to retrieve user information
 */
class SignInUseCase(
    private val authService: AuthService,
    private val creditManager: CreditManager
) {
    /**
     * Retrieves the current user after successful sign-in
     * @return Result with AuthUser on success, exception on failure
     */
    suspend fun execute(): Result<AuthUser> {
        return try {
            val user = authService.getCurrentUser()
            if (user != null) {
                identifyRevenueCatUser(user.id)
                creditManager.loadCredits()
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to retrieve user after sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun identifyRevenueCatUser(userId: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            PurchasesManager.logIn(
                appUserId = userId,
                onSuccess = { continuation.resume(Unit) },
                onError = { continuation.resume(Unit) }
            )
        }
    }
}
