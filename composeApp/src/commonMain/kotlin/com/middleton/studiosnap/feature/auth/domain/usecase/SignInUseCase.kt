package com.middleton.studiosnap.feature.auth.domain.usecase

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.purchases.PurchasesIdentifier

/**
 * Use case for handling the post-sign-in flow.
 * Identifies the user with RevenueCat and loads credits.
 * RevenueCat identity MUST succeed — purchases on an anonymous ID
 * will grant credits to the wrong customer.
 */
class SignInUseCase(
    private val authService: AuthService,
    private val creditManager: CreditManager,
    private val purchasesIdentifier: PurchasesIdentifier
) {
    suspend fun execute(): Result<AuthUser> {
        return try {
            val user = authService.getCurrentUser()
                ?: return Result.failure(Exception("Failed to retrieve user after sign-in"))

            purchasesIdentifier.identifyUser(user.id).getOrThrow()
            creditManager.loadCredits()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
