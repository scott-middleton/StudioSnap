package com.middleton.studiosnap.feature.processing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerateBatchPreviewsUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationProgressStages
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.processing.presentation.action.ProcessingUiAction
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.ui_state.CounterMode
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingStatus
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProcessingViewModel(
    private val generationConfigHolder: GenerationConfigHolder,
    private val generationResultsHolder: GenerationResultsHolder,
    private val generateBatchPreviewsUseCase: GenerateBatchPreviewsUseCase,
    private val analyticsService: AnalyticsService,
    private val completionDelayMs: Long = COMPLETION_DELAY_MS
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProcessingUiState>(ProcessingUiState.Loading)
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<ProcessingNavigationAction?>(null)
    val navigationEvent: StateFlow<ProcessingNavigationAction?> = _navigationEvent.asStateFlow()

    private var processingJob: Job? = null
    private var config: GenerationConfig? = null

    init {
        config = generationConfigHolder.currentConfig
        startProcessing()
    }

    fun handleAction(action: ProcessingUiAction) {
        when (action) {
            ProcessingUiAction.OnRetryClicked -> startProcessing()
            ProcessingUiAction.OnCancelClicked -> {
                _navigationEvent.value = ProcessingNavigationAction.GoBack
            }
            ProcessingUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private inline fun updateProcessing(block: ProcessingUiState.Processing.() -> ProcessingUiState.Processing) {
        _uiState.update { state -> (state as? ProcessingUiState.Processing)?.block() ?: state }
    }

    companion object {
        // Pause at 100% so the progress animation visibly reaches the end before navigating.
        // Without this, a fast download completes before Compose renders the final value.
        private const val COMPLETION_DELAY_MS = 1800L
    }

    private fun startProcessing() {
        val currentConfig = config
        if (currentConfig == null) {
            _uiState.value = ProcessingUiState.Error("No generation config found")
            return
        }

        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            val units = currentConfig.units
            val counterMode = if (currentConfig.photos.size == 1 && currentConfig.styles.size > 1) {
                CounterMode.STYLES
            } else {
                CounterMode.PHOTOS
            }

            _uiState.value = ProcessingUiState.Processing(
                currentUnitIndex = 0,
                totalUnits = units.size,
                styleName = units.firstOrNull()?.style?.displayName
                    ?: currentConfig.styles.first().displayName,
                status = ProcessingStatus.Preparing,
                currentPhotoUri = units.firstOrNull()?.photo?.localUri,
                counterMode = counterMode
            )

            try {
                generateBatchPreviewsUseCase(currentConfig) { unitIndex, unitProgress ->
                    val status = when {
                        unitProgress < GenerationProgressStages.GENERATING_START -> ProcessingStatus.Preparing
                        unitProgress < GenerationProgressStages.DOWNLOADING_START -> ProcessingStatus.Generating
                        else -> ProcessingStatus.Downloading
                    }
                    val unit = units.getOrNull(unitIndex)
                    updateProcessing {
                        copy(
                            currentUnitIndex = unitIndex,
                            status = status,
                            progress = unitProgress,
                            currentPhotoUri = unit?.photo?.localUri,
                            styleName = unit?.style?.displayName ?: styleName
                        )
                    }
                }.collect { batchProgress ->
                    if (batchProgress.isComplete) {
                        generationResultsHolder.currentResults = batchProgress.results
                        generationResultsHolder.refundedCredits = batchProgress.refundedCredits
                        analyticsService.logEvent(
                            AnalyticsEvents.BATCH_GENERATION_COMPLETED,
                            mapOf(
                                "total" to batchProgress.totalCount.toString(),
                                "success" to batchProgress.successCount.toString(),
                                "failure" to batchProgress.failureCount.toString()
                            )
                        )
                        // Force 100% before navigating so the progress animation has
                        // time to reach the end — download can complete so fast that
                        // Compose never renders the intermediate values.
                        updateProcessing { copy(progress = 1f, status = ProcessingStatus.Downloading) }
                        delay(completionDelayMs)
                        _uiState.value = ProcessingUiState.Complete
                        _navigationEvent.value = ProcessingNavigationAction.GoToResults
                    } else {
                        // Unit complete — reset progress for next unit
                        val nextIndex = batchProgress.currentIndex + 1
                        val nextUnit = units.getOrNull(nextIndex)
                        updateProcessing {
                            copy(
                                currentUnitIndex = nextIndex,
                                status = ProcessingStatus.Preparing,
                                progress = null,
                                currentPhotoUri = nextUnit?.photo?.localUri,
                                styleName = nextUnit?.style?.displayName ?: styleName
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = ProcessingUiState.Error(
                    e.message ?: "Generation failed"
                )
                analyticsService.logEvent(
                    AnalyticsEvents.BATCH_GENERATION_FAILED,
                    mapOf("error" to (e.message ?: "unknown"))
                )
            }
        }
    }
}
