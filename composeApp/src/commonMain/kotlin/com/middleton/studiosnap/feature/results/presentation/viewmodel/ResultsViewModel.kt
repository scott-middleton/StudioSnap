package com.middleton.studiosnap.feature.results.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsParams
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.results.domain.usecase.SaveToGalleryUseCase
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import studiosnap.composeapp.generated.resources.Res
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
    }

    fun handleAction(action: ResultsUiAction) {
        when (action) {
            is ResultsUiAction.OnSaveClicked -> saveResult(action.generationId)
            is ResultsUiAction.OnShareClicked -> shareResult(action.generationId)
            is ResultsUiAction.OnToggleBeforeAfter -> toggleBeforeAfter(action.generationId)
            ResultsUiAction.OnSaveAllClicked -> saveAll()
            ResultsUiAction.OnBackClicked -> _navigationEvent.value = ResultsNavigationAction.GoBack
            ResultsUiAction.OnDoneClicked -> _navigationEvent.value = ResultsNavigationAction.GoToHome
            ResultsUiAction.OnSnackbarDismissed -> _uiState.update { it.copy(snackbarMessage = null) }
            ResultsUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private fun loadResults() {
        val results = generationResultsHolder.currentResults ?: return
        _uiState.update {
            it.copy(results = results.map { result -> ResultItem(result = result) })
        }
    }

    private fun toggleBeforeAfter(generationId: String) {
        updateResultItem(generationId) { it.copy(showingOriginal = !it.showingOriginal) }
    }

    private fun saveResult(generationId: String) {
        val item = findSuccessItem(generationId) ?: return
        if (item.isSaved || item.isSaving) return
        val result = item.result as GenerationResult.Success

        setSaving(generationId, true)

        viewModelScope.launch {
            saveToGalleryUseCase(result.previewUri, "studiosnap_$generationId")
                .onSuccess {
                    updateResultItem(generationId) { it.copy(isSaved = true, isSaving = false) }
                    analyticsService.logEvent(AnalyticsEvents.DOWNLOAD_COMPLETED)
                }
                .onFailure { throwable ->
                    setSaving(generationId, false)
                    analyticsService.logEvent(
                        AnalyticsEvents.DOWNLOAD_FAILED,
                        mapOf(AnalyticsParams.ERROR_TYPE to (throwable.message ?: "unknown"))
                    )
                    _uiState.update { it.copy(snackbarMessage = UiText.StringResource(Res.string.results_save_failed)) }
                }
        }
    }

    private fun saveAll() {
        val saveable = _uiState.value.results.filter {
            it.result is GenerationResult.Success && !it.isSaved && !it.isSaving
        }
        if (saveable.isEmpty()) return

        saveable.forEach { item ->
            val id = (item.result as GenerationResult.Success).generationId
            saveResult(id)
        }
    }

    private fun shareResult(generationId: String) {
        analyticsService.logEvent(
            AnalyticsEvents.PREVIEW_SHARED,
            mapOf("generation_id" to generationId)
        )
    }

    private fun findSuccessItem(generationId: String): ResultItem? {
        return _uiState.value.results.find {
            it.result is GenerationResult.Success &&
                    (it.result as GenerationResult.Success).generationId == generationId
        }
    }

    private fun setSaving(generationId: String, saving: Boolean) {
        updateResultItem(generationId) { it.copy(isSaving = saving) }
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
}
