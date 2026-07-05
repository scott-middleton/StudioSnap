package com.middleton.studiosnap.core.domain.usecase

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.WelcomeCreditGranter

/**
 * Claims the one-time welcome credit grant (server-idempotent — safe to call on
 * every sign-in) then refreshes the cached credit balance. Claim failures are
 * non-fatal: the grant is retried on the next call site (e.g. next app launch).
 */
class EnsureWelcomeCreditsUseCase(
    private val welcomeCreditGranter: WelcomeCreditGranter,
    private val creditManager: CreditManager
) {
    suspend operator fun invoke(): Result<UserCredits> {
        runCatching { welcomeCreditGranter.claimWelcomeCredits() }
        return creditManager.refreshCredits()
    }
}
