package com.middleton.studiosnap.core.domain.usecase

import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart

/**
 * Observes the user's credit loading state, reacting to auth changes automatically.
 *
 * When signed out: emits [UserCreditLoadingState.LoggedOut].
 * When signed in: emits [UserCreditLoadingState.Loading] immediately, then calls
 * [CreditManager.loadCredits] (suspending) before subscribing to
 * [CreditManager.credits] + [CreditManager.isLoading] via combine.
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
                combine(
                    creditManager.credits,
                    creditManager.isLoading
                ) { credits, isLoading ->
                    when {
                        isLoading && credits == null -> UserCreditLoadingState.Loading
                        credits != null -> UserCreditLoadingState.Loaded(credits)
                        else -> UserCreditLoadingState.Error
                    }
                }.onStart {
                    emit(UserCreditLoadingState.Loading)
                    creditManager.loadCredits()
                }
            }
        }
    }
}
