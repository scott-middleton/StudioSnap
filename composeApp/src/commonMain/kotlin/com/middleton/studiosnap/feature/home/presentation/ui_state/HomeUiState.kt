package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style

data class HomeUiState(
    val photos: List<ProductPhoto> = emptyList(),
    /**
     * The chosen background: preset styles OR a custom description, mutually exclusive.
     * Null until the user picks either. Single source of truth — [selectedStyles] and the
     * custom text are derived from it.
     */
    val backgroundChoice: BackgroundChoice? = null,
    val isCustomDescriptionExpanded: Boolean = false,
    val showGalleryPicker: Boolean = false,
    val shadow: Boolean = false,
    val reflection: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.DEFAULT,
    val creditLoadingState: UserCreditLoadingState = UserCreditLoadingState.LoggedOut,
    val showSignIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val isGenerating: Boolean = false,
    val pendingGeneration: Boolean = false,
    val recentGenerations: List<HistoryItem> = emptyList(),
    val error: HomeError? = null
) {
    /** The selected preset styles, or empty when no presets are chosen (custom or nothing). */
    val selectedStyles: List<Style>
        get() = (backgroundChoice as? BackgroundChoice.MultiPreset)?.styles ?: emptyList()

    val primaryStyle: Style? get() = selectedStyles.firstOrNull()

    val isSignedIn: Boolean
        get() = creditLoadingState != UserCreditLoadingState.LoggedOut

    val isLoadingCredits: Boolean
        get() = creditLoadingState == UserCreditLoadingState.Loading

    val creditBalance: Int
        get() = (creditLoadingState as? UserCreditLoadingState.Loaded)?.credits?.amount ?: 0

    /**
     * Cost in credits = photos × styles. The style dimension is floored at 1 so the cost
     * (and the affordability checks derived from it) reflects "at least one style/background"
     * before the user has picked anything — and a custom description always counts as a
     * single background applied to every photo.
     */
    val generationCost: Int get() = photos.size * maxOf(selectedStyles.size, 1)

    val canAffordGeneration: Boolean
        get() = creditLoadingState is UserCreditLoadingState.Loaded &&
                (creditLoadingState as UserCreditLoadingState.Loaded).credits.amount >= generationCost

    val canGenerate: Boolean
        get() = photos.isNotEmpty() && isBackgroundChoiceUsable && isSignedIn && canAffordGeneration

    /**
     * Whether the current background choice is complete enough to generate: any preset
     * selection is usable; a custom description must clear the minimum length.
     */
    val isBackgroundChoiceUsable: Boolean
        get() = when (val choice = backgroundChoice) {
            is BackgroundChoice.MultiPreset -> choice.styles.isNotEmpty()
            is BackgroundChoice.Custom -> choice.description.trim().length >= MIN_CUSTOM_DESCRIPTION_LENGTH
            null -> false
        }

    val hasPhotos: Boolean
        get() = photos.isNotEmpty()

    val photoCount: Int
        get() = photos.size

    /**
     * Style multi-select is only available when exactly 1 photo is selected. 2+ photos
     * force single-select (a batch is either N photos × 1 style or 1 photo × N styles).
     */
    val styleMaxSelectable: Int
        get() = if (photos.size <= 1) MAX_STYLES else 1

    val isMultiStyle: Boolean get() = selectedStyles.size > 1

    companion object {
        const val MAX_PHOTOS = 10
        const val MAX_STYLES = 4
        const val MAX_CUSTOM_DESCRIPTION_LENGTH = 150
        const val MIN_CUSTOM_DESCRIPTION_LENGTH = 3
    }
}

sealed interface HomeError {
    data object TooManyPhotos : HomeError
    data object GenerationFailed : HomeError
    data object SignInFailed : HomeError
    data object CreditsUnavailable : HomeError
}
