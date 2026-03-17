package com.middleton.studiosnap.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class HomeViewModel(
    private val styleRepository: StyleRepository,
    private val creditQueries: CreditQueries,
    private val authService: AuthService,
    private val generationConfigHolder: GenerationConfigHolder,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<HomeNavigationAction?>(null)
    val navigationEvent: StateFlow<HomeNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        loadInitialState()
        observeCredits()
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
            is HomeUiAction.OnShadowToggled -> toggleShadow(action.enabled)
            is HomeUiAction.OnReflectionToggled -> toggleReflection(action.enabled)
            is HomeUiAction.OnExportFormatSelected -> selectExportFormat(action.format)
            is HomeUiAction.OnStylePickerClicked -> navigateTo(
                HomeNavigationAction.GoToStylePicker(_uiState.value.selectedStyle?.id)
            )
            is HomeUiAction.OnGenerateClicked -> onGenerate()
            is HomeUiAction.OnSettingsClicked -> navigateTo(HomeNavigationAction.GoToSettings)
            is HomeUiAction.OnHistoryClicked -> navigateTo(HomeNavigationAction.GoToHistory)
            is HomeUiAction.OnCreditBalanceClicked -> navigateTo(HomeNavigationAction.GoToCreditStore)
            is HomeUiAction.OnErrorDismissed -> _uiState.update { it.copy(error = null) }
            is HomeUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSignedIn = authService.isSignedIn.value)
            }
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            creditQueries.observeCredits().collect { credits ->
                _uiState.update { it.copy(creditBalance = credits.amount) }
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
        _uiState.update { it.copy(selectedStyle = style) }
        if (style != null) {
            analyticsService.logEvent(
                AnalyticsEvents.STYLE_SELECTED,
                mapOf("style_id" to styleId, "category" to style.categories.first().name)
            )
        }
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

    private fun onGenerate() {
        val state = _uiState.value
        val style = state.selectedStyle ?: return

        if (state.photos.isEmpty()) return

        val config = GenerationConfig(
            photos = state.photos,
            style = style,
            shadow = state.shadow,
            reflection = state.reflection,
            exportFormat = state.exportFormat,
            quality = GenerationQuality.DEFAULT
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

        _navigationEvent.value = HomeNavigationAction.GoToProcessing
    }

    private fun navigateTo(action: HomeNavigationAction) {
        _navigationEvent.value = action
    }
}
