package com.middleton.studiosnap.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.usecase.EnsureWelcomeCreditsUseCase
import com.middleton.studiosnap.core.domain.usecase.ObserveCreditStateUseCase
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.usecase.BuildKontextPromptUseCase
import com.middleton.studiosnap.core.presentation.util.asDisplayString
import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.BackgroundChoice
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock

class HomeViewModel(
    private val styleRepository: StyleRepository,
    private val observeCreditStateUseCase: ObserveCreditStateUseCase,
    private val generationConfigHolder: GenerationConfigHolder,
    private val analyticsService: AnalyticsService,
    private val historyRepository: HistoryRepository,
    private val ensureWelcomeCreditsUseCase: EnsureWelcomeCreditsUseCase,
    private val buildKontextPromptUseCase: BuildKontextPromptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<HomeNavigationAction?>(null)
    val navigationEvent: StateFlow<HomeNavigationAction?> = _navigationEvent.asStateFlow()

    // True for the whole post-sign-in claim -> affordability check -> generate
    // sequence in onSignInResult, which spans two network calls. Returning from
    // the native sign-in sheet can trigger an OnScreenResumed dispatch mid-sequence
    // (e.g. Android activity resume) — without this guard, handleScreenResumed()
    // would reset isGenerating to false and let the user tap Generate again while
    // the first attempt is still in flight.
    private var isCompletingSignInFlow = false

    init {
        observeCreditState()
        observeRecentGenerations()
    }

    fun handleAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.OnAddPhotosClicked -> showGalleryPicker()
            is HomeUiAction.OnPhotoPickerCancelled -> dismissGalleryPicker()
            is HomeUiAction.OnPhotosSelected -> {
                dismissGalleryPicker()
                addPhotos(action.uris)
            }
            is HomeUiAction.OnPhotoRemoved -> removePhoto(action.photoId)
            is HomeUiAction.OnStylesSelected -> selectStyles(action.styleIds)
            is HomeUiAction.OnCustomDescriptionChanged -> onCustomDescriptionChanged(action.text)
            is HomeUiAction.OnCustomDescriptionExpandedToggled -> toggleCustomDescriptionExpanded()
            is HomeUiAction.OnShadowToggled -> toggleShadow(action.enabled)
            is HomeUiAction.OnReflectionToggled -> toggleReflection(action.enabled)
            is HomeUiAction.OnExportFormatSelected -> selectExportFormat(action.format)
            is HomeUiAction.OnStylePickerClicked -> navigateTo(
                HomeNavigationAction.GoToStylePicker(
                    currentStyleIds = _uiState.value.selectedStyles.map { it.id },
                    maxSelectable = _uiState.value.styleMaxSelectable
                )
            )
            is HomeUiAction.OnGenerateClicked -> onGenerateClicked()
            is HomeUiAction.OnSignInResult -> onSignInResult(action.success)
            is HomeUiAction.OnSettingsClicked -> navigateTo(HomeNavigationAction.GoToSettings)
            is HomeUiAction.OnHistoryClicked -> navigateTo(HomeNavigationAction.GoToHistory)
            is HomeUiAction.OnViewAllHistoryClicked -> navigateTo(HomeNavigationAction.GoToHistory)
            is HomeUiAction.OnRecentGenerationClicked -> navigateTo(HomeNavigationAction.GoToHistory)
            is HomeUiAction.OnCreditBalanceClicked -> onCreditBalanceClicked()
            is HomeUiAction.OnErrorDismissed -> _uiState.update { it.copy(error = null) }
            is HomeUiAction.OnNavigationHandled -> _navigationEvent.value = null
            is HomeUiAction.OnScreenResumed -> handleScreenResumed()
        }
    }

    private fun handleScreenResumed() {
        _uiState.update {
            it.copy(
                showGalleryPicker = false,
                showSignIn = false,
                isSigningIn = false,
                isGenerating = if (isCompletingSignInFlow) it.isGenerating else false
            )
        }
    }

    private fun onCreditBalanceClicked() {
        if (!_uiState.value.isSignedIn) {
            _uiState.update { it.copy(showSignIn = true, isSigningIn = true, pendingGeneration = false) }
        } else {
            navigateTo(HomeNavigationAction.GoToCreditStore)
        }
    }

    private fun observeCreditState() {
        observeCreditStateUseCase()
            .onEach { creditState ->
                _uiState.update { it.copy(creditLoadingState = creditState) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeRecentGenerations() {
        viewModelScope.launch {
            historyRepository.getAll().collect { results ->
                val items = results.take(RECENT_GENERATIONS_MAX).map { result ->
                    HistoryItem(
                        id = result.generationId,
                        previewUri = result.previewUri,
                        styleName = result.styleDisplayName.asDisplayString(),
                        createdAt = result.createdAt
                    )
                }
                _uiState.update { it.copy(recentGenerations = items) }
            }
        }
    }

    private fun showGalleryPicker() {
        val currentPhotos = _uiState.value.photos
        if (currentPhotos.size >= HomeUiState.MAX_PHOTOS) {
            _uiState.update { it.copy(error = HomeError.TooManyPhotos) }
            return
        }
        _uiState.update { it.copy(showGalleryPicker = true) }
    }

    private fun dismissGalleryPicker() {
        _uiState.update { it.copy(showGalleryPicker = false) }
    }

    private fun addPhotos(uris: List<String>) {
        val currentPhotos = _uiState.value.photos
        val remainingSlots = HomeUiState.MAX_PHOTOS - currentPhotos.size
        if (remainingSlots <= 0) return

        val newPhotos = uris.take(remainingSlots).mapIndexed { index, uri ->
            ProductPhoto(
                id = "${Clock.System.now().toEpochMilliseconds()}_$index",
                localUri = uri
            )
        }

        val updatedPhotos = currentPhotos + newPhotos
        _uiState.update { state ->
            // Adding a 2nd+ photo while multiple preset styles are selected collapses the
            // selection to the first selected style (multi-style requires exactly 1 photo).
            // A custom description is unaffected — it applies to every photo.
            val choice = state.backgroundChoice
            val collapsedChoice = if (
                updatedPhotos.size > 1 &&
                choice is BackgroundChoice.MultiPreset &&
                choice.styles.size > 1
            ) {
                BackgroundChoice.MultiPreset(choice.styles.take(1))
            } else {
                choice
            }
            state.copy(photos = updatedPhotos, backgroundChoice = collapsedChoice)
        }
        analyticsService.logEvent(AnalyticsEvents.PHOTO_ADDED, mapOf("count" to newPhotos.size.toString()))
    }

    private fun removePhoto(photoId: String) {
        _uiState.update { state ->
            state.copy(photos = state.photos.filter { it.id != photoId })
        }
    }

    private fun selectStyles(styleIds: List<String>) {
        // Selecting presets is mutually exclusive with a custom description — this replaces
        // whatever background choice was active. An empty selection clears back to null.
        val styles = styleIds.mapNotNull { styleRepository.getStyleById(it) }.take(HomeUiState.MAX_STYLES)
        _uiState.update {
            it.copy(backgroundChoice = if (styles.isEmpty()) null else BackgroundChoice.MultiPreset(styles))
        }
        styles.firstOrNull()?.let { style ->
            analyticsService.logEvent(
                AnalyticsEvents.STYLE_SELECTED,
                mapOf("style_id" to style.id, "category" to style.categories.first().name)
            )
        }
    }

    private fun onCustomDescriptionChanged(text: String) {
        // A custom description is mutually exclusive with preset styles — typing one
        // replaces any preset selection; clearing it resets the background choice to null.
        val capped = text.take(HomeUiState.MAX_CUSTOM_DESCRIPTION_LENGTH)
        _uiState.update {
            it.copy(backgroundChoice = if (capped.isBlank()) null else BackgroundChoice.Custom(capped))
        }
    }

    private fun toggleCustomDescriptionExpanded() {
        _uiState.update { it.copy(isCustomDescriptionExpanded = !it.isCustomDescriptionExpanded) }
    }

    private fun toggleShadow(enabled: Boolean) {
        _uiState.update { it.copy(shadow = enabled) }
    }

    private fun toggleReflection(enabled: Boolean) {
        _uiState.update { it.copy(reflection = enabled) }
    }

    private fun selectExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(exportFormat = format) }
        analyticsService.logEvent(
            AnalyticsEvents.EXPORT_FORMAT_SELECTED,
            mapOf("format" to format.name)
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun onGenerateClicked() {
        val state = _uiState.value

        // Sign-in takes priority - user should sign in first
        if (!state.isSignedIn) {
            _uiState.update { it.copy(showSignIn = true, isSigningIn = true, pendingGeneration = true) }
            return
        }

        if (state.creditLoadingState == UserCreditLoadingState.Error) {
            _uiState.update { it.copy(error = HomeError.CreditsUnavailable) }
            return
        }

        val choice = state.backgroundChoice ?: return
        if (!state.isBackgroundChoiceUsable) return
        if (state.photos.isEmpty()) return

        if (!state.canAffordGeneration) {
            navigateTo(HomeNavigationAction.GoToCreditStore)
            return
        }

        startGeneration(state, choice)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun startGeneration(state: HomeUiState, choice: BackgroundChoice) {
        val units = buildUnits(state, choice)
        val config = GenerationConfig(
            photos = state.photos,
            units = units,
            shadow = state.shadow,
            reflection = state.reflection,
            exportFormat = state.exportFormat,
            quality = GenerationQuality.DEFAULT,
            batchId = Uuid.random().toString()
        )

        generationConfigHolder.currentConfig = config
        val styles = config.styles
        analyticsService.logEvent(
            AnalyticsEvents.PREVIEW_GENERATION_STARTED,
            mapOf(
                "style_id" to (styles.firstOrNull()?.id ?: ""),
                "style_count" to styles.size.toString(),
                "photo_count" to state.photos.size.toString(),
                "export_format" to state.exportFormat.name,
                "shadow" to state.shadow.toString(),
                "reflection" to state.reflection.toString()
            )
        )

        _uiState.update { it.copy(isGenerating = true) }
        _navigationEvent.value = HomeNavigationAction.GoToProcessing
    }

    /**
     * Expands the mutually-exclusive background [choice] into the batch's atomic units:
     *  - [BackgroundChoice.MultiPreset]: cartesian product photo-major (all styles for
     *    photo 0, then photo 1, ...), each unit's prompt built from the style's Kontext
     *    prompt plus shadow/reflection modifiers.
     *  - [BackgroundChoice.Custom]: one unit per photo sharing a single placeholder Style
     *    and the same custom-description prompt (History/Results show a generic label; the
     *    user's text is never persisted).
     */
    private fun buildUnits(
        state: HomeUiState,
        choice: BackgroundChoice
    ): List<GenerationConfig.GenerationUnit> = when (choice) {
        is BackgroundChoice.MultiPreset -> state.photos.flatMap { photo ->
            choice.styles.map { style ->
                GenerationConfig.GenerationUnit(
                    photo = photo,
                    style = style,
                    resolvedPrompt = buildKontextPromptUseCase(style, state.shadow, state.reflection)
                )
            }
        }
        is BackgroundChoice.Custom -> {
            val placeholderStyle = Style(
                id = CUSTOM_STYLE_ID,
                displayName = UiText.DynamicString(CUSTOM_BACKGROUND_LABEL),
                categories = emptySet(),
                thumbnail = null,
                kontextPrompt = ""
            )
            val prompt = buildKontextPromptUseCase(choice.description.trim(), state.shadow, state.reflection)
            state.photos.map { photo ->
                GenerationConfig.GenerationUnit(photo, placeholderStyle, prompt)
            }
        }
    }

    private fun onSignInResult(success: Boolean) {
        val wasPendingGeneration = _uiState.value.pendingGeneration
        _uiState.update {
            it.copy(
                showSignIn = false,
                isSigningIn = false,
                pendingGeneration = false,
                error = if (success) it.error else HomeError.SignInFailed
            )
        }

        if (!success) {
            analyticsService.logEvent(AnalyticsEvents.SIGN_IN_FAILED)
            return
        }

        if (!wasPendingGeneration) return

        viewModelScope.launch {
            isCompletingSignInFlow = true
            try {
                _uiState.update { it.copy(isGenerating = true) }
                val creditsResult = ensureWelcomeCreditsUseCase()
                val state = _uiState.value
                val choice = state.backgroundChoice

                if (choice == null || !state.isBackgroundChoiceUsable || state.photos.isEmpty()) {
                    _uiState.update { it.copy(isGenerating = false) }
                    return@launch
                }

                // On refresh failure, fall back to the live credit state rather than
                // treating the balance as 0 — an already-funded user shouldn't be
                // misrouted to the credit store just because this one refresh failed.
                val balance = creditsResult.getOrNull()?.amount ?: state.creditBalance

                if (balance < state.generationCost) {
                    _uiState.update { it.copy(isGenerating = false) }
                    navigateTo(HomeNavigationAction.GoToCreditStore)
                    return@launch
                }

                startGeneration(state, choice)
            } finally {
                isCompletingSignInFlow = false
            }
        }
    }

    private fun navigateTo(action: HomeNavigationAction) {
        _navigationEvent.value = action
    }

    companion object {
        private const val RECENT_GENERATIONS_MAX = 5
        private const val CUSTOM_STYLE_ID = "custom"

        // Fixed placeholder label for a custom-description generation — never the user's
        // actual text (decided: History/Results always show this generic label, and the
        // text itself is never persisted). Plain constant, not UiText.StringResource: this
        // Style flows into GenerationMapper.toEntity() (a non-suspend function with no
        // resource-loading context) for Room persistence, so the text must already be
        // resolved. Mirrors the existing placeholder-Style pattern in
        // GenerationMapper.toDomainModel()'s "style was deleted" fallback.
        private const val CUSTOM_BACKGROUND_LABEL = "Custom background"
    }
}
