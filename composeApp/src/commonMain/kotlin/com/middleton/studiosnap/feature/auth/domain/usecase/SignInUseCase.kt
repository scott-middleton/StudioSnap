package com.middleton.studiosnap.feature.auth.domain.usecase

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.usecase.EnsureWelcomeCreditsUseCase
import com.middleton.studiosnap.purchases.PurchasesIdentifier

/**
 * Use case for handling the post-sign-in flow.
 * Identifies the user with RevenueCat, then claims the welcome credit grant
 * (server-idempotent, so a no-op on returning users) and loads credits.
 * RevenueCat identity MUST succeed — purchases on an anonymous ID
 * will grant credits to the wrong customer.
 */
class SignInUseCase(
    private val authService: AuthService,
    private val ensureWelcomeCreditsUseCase: EnsureWelcomeCreditsUseCase,
    private val purchasesIdentifier: PurchasesIdentifier
) {
    suspend fun execute(): Result<AuthUser> {
        return try {
            val user = authService.getCurrentUser()
                ?: return Result.failure(Exception("Failed to retrieve user after sign-in"))

            purchasesIdentifier.identifyUser(user.id).getOrThrow()
            ensureWelcomeCreditsUseCase()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
