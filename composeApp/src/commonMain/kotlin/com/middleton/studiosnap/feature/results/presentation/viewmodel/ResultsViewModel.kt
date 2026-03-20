package com.middleton.studiosnap.feature.results.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.presentation.util.shareImage
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsParams
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.results.domain.usecase.SaveToGalleryUseCase
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.results_credits_refunded
import studiosnap.composeapp.generated.resources.results_save_failed
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultItem
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val generationResultsHolder: GenerationResultsHolder,
    private val saveToGalleryUseCase: SaveToGalleryUseCase,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<ResultsNavigationAction?>(null)
    val navigationEvent: StateFlow<ResultsNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        loadResults()
        autoSaveAllToGallery()
    }

    fun handleAction(action: ResultsUiAction) {
        when (action) {
            is ResultsUiAction.OnShareClicked -> shareResult(action.generationId)
            is ResultsUiAction.OnToggleBeforeAfter -> toggleBeforeAfter(action.generationId)
            ResultsUiAction.OnBackClicked -> _navigationEvent.value = ResultsNavigationAction.GoBack
            ResultsUiAction.OnDoneClicked -> _navigationEvent.value = ResultsNavigationAction.GoToHome
            ResultsUiAction.OnSnackbarDismissed -> _uiState.update { it.copy(snackbarMessage = null) }
            ResultsUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private fun loadResults() {
        val results = generationResultsHolder.currentResults ?: return
        val refunded = generationResultsHolder.refundedCredits
        val totalCount = results.size
        val refundSnackbar = if (refunded > 0) {
            UiText.StringResource(Res.string.results_credits_refunded, arrayOf(refunded, totalCount))
        } else null
        _uiState.update {
            it.copy(
                results = results.map { result -> ResultItem(result = result) },
                creditsRefunded = refunded,
                snackbarMessage = refundSnackbar
            )
        }
    }

    private fun autoSaveAllToGallery() {
        val results = _uiState.value.results
        if (results.isEmpty()) return

        _uiState.update { it.copy(isAutoSaving = true) }
        viewModelScope.launch {
            results.forEach { item ->
                val success = item.result as? GenerationResult.Success ?: return@forEach
                saveToGalleryUseCase(success.previewUri, "$SAVE_NAME_PREFIX${success.generationId}")
                    .onSuccess {
                        updateResultItem(success.generationId) { it.copy(isSavedToGallery = true) }
                        analyticsService.logEvent(AnalyticsEvents.DOWNLOAD_COMPLETED)
                    }
                    .onFailure { throwable ->
                        analyticsService.logEvent(
                            AnalyticsEvents.DOWNLOAD_FAILED,
                            mapOf(AnalyticsParams.ERROR_TYPE to (throwable.message ?: "unknown"))
                        )
                        _uiState.update {
                            it.copy(snackbarMessage = UiText.StringResource(Res.string.results_save_failed))
                        }
                    }
            }
            _uiState.update { it.copy(isAutoSaving = false) }
        }
    }

    private fun toggleBeforeAfter(generationId: String) {
        updateResultItem(generationId) { it.copy(showingOriginal = !it.showingOriginal) }
    }

    private fun shareResult(generationId: String) {
        val item = _uiState.value.results.find {
            it.result is GenerationResult.Success &&
                    (it.result as GenerationResult.Success).generationId == generationId
        } ?: return
        val result = item.result as GenerationResult.Success

        viewModelScope.launch {
            shareImage(result.previewUri)
                .onSuccess {
                    analyticsService.logEvent(
                        AnalyticsEvents.PREVIEW_SHARED,
                        mapOf(AnalyticsParams.GENERATION_ID to generationId)
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(snackbarMessage = UiText.DynamicString(throwable.message ?: "Share failed"))
                    }
                }
        }
    }

    private fun updateResultItem(generationId: String, transform: (ResultItem) -> ResultItem) {
        _uiState.update { state ->
            state.copy(
                results = state.results.map { item ->
                    if (item.result is GenerationResult.Success &&
                        (item.result as GenerationResult.Success).generationId == generationId
                    ) {
                        transform(item)
                    } else {
                        item
                    }
                }
            )
        }
    }

    companion object {
        private const val SAVE_NAME_PREFIX = "studiosnap_"
    }
}
