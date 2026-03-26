package com.middleton.studiosnap.core.domain.usecase

import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Observes the user's credit loading state, reacting to auth changes automatically.
 *
 * When signed out: emits [UserCreditLoadingState.LoggedOut].
 * When signed in: triggers a credits fetch, then emits Loading → Loaded/Error
 * based on [CreditManager.isLoading] and [CreditManager.credits].
 */
class ObserveCreditStateUseCase(
    private val authService: AuthService,
    private val creditManager: CreditManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<UserCreditLoadingState> {
        return authService.isSignedIn.flatMapLatest { signedIn ->
            if (!signedIn) {
                flowOf(UserCreditLoadingState.LoggedOut)
            } else {
                creditManager.loadCredits()
                combine(
                    creditManager.credits,
                    creditManager.isLoading
                ) { credits, isLoading ->
                    when {
                        isLoading && credits == null -> UserCreditLoadingState.Loading
                        credits != null -> UserCreditLoadingState.Loaded(credits)
                        else -> UserCreditLoadingState.Error
                    }
                }
            }
        }
    }
}
