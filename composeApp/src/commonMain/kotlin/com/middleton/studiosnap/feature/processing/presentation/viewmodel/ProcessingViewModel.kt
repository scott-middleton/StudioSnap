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
            val firstPhotoUri = currentConfig.photos.firstOrNull()?.localUri

            _uiState.value = ProcessingUiState.Processing(
                currentPhotoIndex = 0,
                totalPhotos = currentConfig.photos.size,
                styleName = currentConfig.style.displayName,
                status = ProcessingStatus.Preparing,
                currentPhotoUri = firstPhotoUri
            )

            try {
                generateBatchPreviewsUseCase(currentConfig) { photoIndex, photoProgress ->
                    val status = when {
                        photoProgress < GenerationProgressStages.GENERATING_START -> ProcessingStatus.Preparing
                        photoProgress < GenerationProgressStages.DOWNLOADING_START -> ProcessingStatus.Generating
                        else -> ProcessingStatus.Downloading
                    }
                    updateProcessing {
                        copy(
                            currentPhotoIndex = photoIndex,
                            status = status,
                            progress = photoProgress,
                            currentPhotoUri = currentConfig.photos.getOrNull(photoIndex)?.localUri
                        )
                    }
                }.collect { batchProgress ->
                    if (batchProgress.isComplete) {
                        generationResultsHolder.currentResults = batchProgress.results
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
                        // Photo complete — reset progress for next photo
                        val nextIndex = batchProgress.currentIndex + 1
                        val nextPhotoUri = currentConfig.photos.getOrNull(nextIndex)?.localUri
                        updateProcessing {
                            copy(
                                currentPhotoIndex = nextIndex,
                                status = ProcessingStatus.Preparing,
                                progress = null,
                                currentPhotoUri = nextPhotoUri
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
