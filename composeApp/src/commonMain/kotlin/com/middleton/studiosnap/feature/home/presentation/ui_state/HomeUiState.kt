package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style

data class HomeUiState(
    val photos: List<ProductPhoto> = emptyList(),
    val selectedStyle: Style? = null,
    val showGalleryPicker: Boolean = false,
    val shadow: Boolean = false,
    val reflection: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.DEFAULT,
    val creditLoadingState: UserCreditLoadingState = UserCreditLoadingState.LoggedOut,
    val showSignIn: Boolean = false,
    val recentGenerations: List<HistoryItem> = emptyList(),
    val error: HomeError? = null
) {
    val isSignedIn: Boolean
        get() = creditLoadingState != UserCreditLoadingState.LoggedOut

    val isLoadingCredits: Boolean
        get() = creditLoadingState == UserCreditLoadingState.Loading

    val creditBalance: Int
        get() = (creditLoadingState as? UserCreditLoadingState.Loaded)?.credits?.amount ?: 0

    val generationCost: Int get() = photos.size

    val canAffordGeneration: Boolean
        get() = creditLoadingState is UserCreditLoadingState.Loaded &&
                (creditLoadingState as UserCreditLoadingState.Loaded).credits.amount >= generationCost

    val canGenerate: Boolean
        get() = photos.isNotEmpty() && selectedStyle != null && isSignedIn && canAffordGeneration

    val hasPhotos: Boolean
        get() = photos.isNotEmpty()

    val photoCount: Int
        get() = photos.size

    companion object {
        const val MAX_PHOTOS = 10
    }
}

sealed interface HomeError {
    data object TooManyPhotos : HomeError
    data object GenerationFailed : HomeError
}
