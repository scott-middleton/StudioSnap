package com.middleton.studiosnap.feature.results.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsParams
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.results.domain.usecase.DownloadFullResUseCase
import com.middleton.studiosnap.feature.results.domain.usecase.InsufficientCreditsException
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultItem
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val generationResultsHolder: GenerationResultsHolder,
    private val downloadFullResUseCase: DownloadFullResUseCase,
    private val creditQueries: CreditQueries,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<ResultsNavigationAction?>(null)
    val navigationEvent: StateFlow<ResultsNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        loadResults()
        observeCredits()
    }

    fun handleAction(action: ResultsUiAction) {
        when (action) {
            is ResultsUiAction.OnDownloadClicked -> downloadResult(action.generationId)
            is ResultsUiAction.OnShareClicked -> shareResult(action.generationId)
            ResultsUiAction.OnDownloadAllClicked -> downloadAll()
            ResultsUiAction.OnBackClicked -> _navigationEvent.value = ResultsNavigationAction.GoBack
            ResultsUiAction.OnDoneClicked -> _navigationEvent.value = ResultsNavigationAction.GoToHome
            ResultsUiAction.OnBuyCreditsClicked -> _navigationEvent.value = ResultsNavigationAction.GoToCreditStore
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

    private fun observeCredits() {
        viewModelScope.launch {
            creditQueries.observeCredits().collect { credits ->
                _uiState.update { it.copy(creditBalance = credits.amount) }
            }
        }
    }

    private fun downloadResult(generationId: String) {
        val item = findSuccessItem(generationId) ?: return
        if (item.isPurchased) return

        setDownloading(generationId, true)

        viewModelScope.launch {
            downloadFullResUseCase(generationId)
                .onSuccess { localUri ->
                    updateResultItem(generationId) {
                        it.copy(isPurchased = true, isDownloading = false, fullResLocalUri = localUri)
                    }
                    analyticsService.logEvent(AnalyticsEvents.DOWNLOAD_COMPLETED)
                    _uiState.update { it.copy(snackbarMessage = "Downloaded successfully") }
                }
                .onFailure { throwable ->
                    setDownloading(generationId, false)
                    analyticsService.logEvent(
                        AnalyticsEvents.DOWNLOAD_FAILED,
                        mapOf(AnalyticsParams.ERROR_TYPE to (throwable.message ?: "unknown"))
                    )
                    val message = when (throwable) {
                        is InsufficientCreditsException -> "Not enough credits"
                        else -> "Download failed"
                    }
                    _uiState.update { it.copy(snackbarMessage = message) }
                }
        }
    }

    private fun downloadAll() {
        val downloadable = _uiState.value.results
            .filter { it.result is GenerationResult.Success && !it.isPurchased }

        if (downloadable.isEmpty()) return

        val creditsNeeded = downloadable.size * DownloadFullResUseCase.DOWNLOAD_CREDIT_COST
        if (_uiState.value.creditBalance < creditsNeeded) {
            _uiState.update { it.copy(snackbarMessage = "Need $creditsNeeded credits to download all") }
            return
        }

        downloadable.forEach { item ->
            val id = (item.result as GenerationResult.Success).generationId
            downloadResult(id)
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

    private fun setDownloading(generationId: String, downloading: Boolean) {
        updateResultItem(generationId) { it.copy(isDownloading = downloading) }
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
