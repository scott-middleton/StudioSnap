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
            is HomeUiAction.OnStyleSelected -> selectStyle(action.styleId)
            is HomeUiAction.OnCustomDescriptionChanged -> onCustomDescriptionChanged(action.text)
            is HomeUiAction.OnCustomDescriptionExpandedToggled -> toggleCustomDescriptionExpanded()
            is HomeUiAction.OnShadowToggled -> toggleShadow(action.enabled)
            is HomeUiAction.OnReflectionToggled -> toggleReflection(action.enabled)
            is HomeUiAction.OnExportFormatSelected -> selectExportFormat(action.format)
            is HomeUiAction.OnStylePickerClicked -> navigateTo(
                HomeNavigationAction.GoToStylePicker(
                    (_uiState.value.backgroundChoice as? BackgroundChoice.Preset)?.style?.id
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
                isGenerating = false
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

        _uiState.update { it.copy(photos = currentPhotos + newPhotos) }
        analyticsService.logEvent(AnalyticsEvents.PHOTO_ADDED, mapOf("count" to newPhotos.size.toString()))
    }

    private fun removePhoto(photoId: String) {
        _uiState.update { state ->
            state.copy(photos = state.photos.filter { it.id != photoId })
        }
    }

    private fun selectStyle(styleId: String) {
        val style = styleRepository.getStyleById(styleId)
        if (style != null) {
            _uiState.update { it.copy(backgroundChoice = BackgroundChoice.Preset(style)) }
            analyticsService.logEvent(
                AnalyticsEvents.STYLE_SELECTED,
                mapOf("style_id" to styleId, "category" to style.categories.first().name)
            )
        }
    }

    private fun onCustomDescriptionChanged(text: String) {
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
        val (style, resolvedPrompt) = when (choice) {
            is BackgroundChoice.Preset -> choice.style to buildKontextPromptUseCase(
                choice.style, state.shadow, state.reflection
            )
            is BackgroundChoice.Custom -> {
                val placeholderStyle = Style(
                    id = CUSTOM_STYLE_ID,
                    displayName = UiText.DynamicString(CUSTOM_BACKGROUND_LABEL),
                    categories = emptySet(),
                    thumbnail = null,
                    kontextPrompt = ""
                )
                placeholderStyle to buildKontextPromptUseCase(
                    choice.description.trim(), state.shadow, state.reflection
                )
            }
        }

        val config = GenerationConfig(
            photos = state.photos,
            style = style,
            resolvedPrompt = resolvedPrompt,
            shadow = state.shadow,
            reflection = state.reflection,
            exportFormat = state.exportFormat,
            quality = GenerationQuality.DEFAULT,
            batchId = Uuid.random().toString()
        )

        generationConfigHolder.currentConfig = config
        analyticsService.logEvent(
            AnalyticsEvents.PREVIEW_GENERATION_STARTED,
            mapOf(
                "style_id" to style.id,
                "photo_count" to state.photos.size.toString(),
                "export_format" to state.exportFormat.name,
                "shadow" to state.shadow.toString(),
                "reflection" to state.reflection.toString()
            )
        )

        _uiState.update { it.copy(isGenerating = true) }
        _navigationEvent.value = HomeNavigationAction.GoToProcessing
    }

    private fun onSignInResult(success: Boolean) {
        val wasPendingGeneration = _uiState.value.pendingGeneration
        _uiState.update {
            it.copy(showSignIn = false, isSigningIn = false, pendingGeneration = false)
        }

        if (!success || !wasPendingGeneration) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val balance = ensureWelcomeCreditsUseCase().getOrNull()?.amount ?: 0
            val state = _uiState.value
            val choice = state.backgroundChoice

            if (choice == null || !state.isBackgroundChoiceUsable || state.photos.isEmpty()) {
                _uiState.update { it.copy(isGenerating = false) }
                return@launch
            }

            if (balance < state.generationCost) {
                _uiState.update { it.copy(isGenerating = false) }
                navigateTo(HomeNavigationAction.GoToCreditStore)
                return@launch
            }

            startGeneration(state, choice)
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
