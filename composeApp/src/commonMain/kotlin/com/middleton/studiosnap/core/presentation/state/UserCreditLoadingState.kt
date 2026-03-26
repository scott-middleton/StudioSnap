package com.middleton.studiosnap.core.presentation.state

import com.middleton.studiosnap.core.domain.model.UserCredits

sealed interface UserCreditLoadingState {
    /** User is not signed in. */
    data object LoggedOut : UserCreditLoadingState

    /** User is signed in but credits haven't loaded yet. */
    data object Loading : UserCreditLoadingState

    /** Credits loaded successfully. */
    data class Loaded(val credits: UserCredits) : UserCreditLoadingState

    /** Credits fetch failed after sign-in. */
    data object Error : UserCreditLoadingState
}
