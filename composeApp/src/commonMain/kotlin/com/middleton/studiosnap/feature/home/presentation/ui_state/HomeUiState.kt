package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style

data class HomeUiState(
    val photos: List<ProductPhoto> = emptyList(),
    val selectedStyles: List<Style> = emptyList(),
    val showGalleryPicker: Boolean = false,
    val shadow: Boolean = false,
    val reflection: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.DEFAULT,
    val creditLoadingState: UserCreditLoadingState = UserCreditLoadingState.LoggedOut,
    val showSignIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val isGenerating: Boolean = false,
    val pendingFreeGeneration: Boolean = false,
    val hasUsedFreeGeneration: Boolean = false,
    val recentGenerations: List<HistoryItem> = emptyList(),
    val error: HomeError? = null
) {
    val primaryStyle: Style? get() = selectedStyles.firstOrNull()

    val isSignedIn: Boolean
        get() = creditLoadingState != UserCreditLoadingState.LoggedOut

    val isLoadingCredits: Boolean
        get() = creditLoadingState == UserCreditLoadingState.Loading

    val creditBalance: Int
        get() = (creditLoadingState as? UserCreditLoadingState.Loaded)?.credits?.amount ?: 0

    /**
     * Cost in credits = photos × styles. The style dimension is floored at 1 so the cost
     * (and the affordability/free-trial checks derived from it) reflects "at least one
     * style" before the user has picked any — matching the pre-multi-style behavior where
     * cost was simply the photo count.
     */
    val generationCost: Int get() = photos.size * maxOf(selectedStyles.size, 1)

    val canAffordGeneration: Boolean
        get() = creditLoadingState is UserCreditLoadingState.Loaded &&
                (creditLoadingState as UserCreditLoadingState.Loaded).credits.amount >= generationCost

    val isFreeTrialMode: Boolean
        get() = !hasUsedFreeGeneration && when (creditLoadingState) {
            UserCreditLoadingState.LoggedOut -> true
            is UserCreditLoadingState.Loaded -> !canAffordGeneration
            UserCreditLoadingState.Loading, UserCreditLoadingState.Error -> false
        }

    val canGenerate: Boolean
        get() = photos.isNotEmpty() && selectedStyles.isNotEmpty() &&
                (isFreeTrialMode || (isSignedIn && canAffordGeneration))

    val hasPhotos: Boolean
        get() = photos.isNotEmpty()

    val photoCount: Int
        get() = photos.size

    /**
     * Style multi-select is only available when exactly 1 photo is selected (and the user
     * isn't in the free trial, which caps the style dimension at 1 regardless of photo count).
     * 2+ photos or free-trial mode force single-select.
     */
    val styleMaxSelectable: Int
        get() = if (photos.size <= 1 && !isFreeTrialMode) MAX_STYLES else 1

    val isMultiStyle: Boolean get() = selectedStyles.size > 1

    companion object {
        const val MAX_PHOTOS = 10
        const val MAX_STYLES = 4
    }
}

sealed interface HomeError {
    data object TooManyPhotos : HomeError
    data object GenerationFailed : HomeError
    data object SignInFailed : HomeError
    data object CreditsUnavailable : HomeError
}
