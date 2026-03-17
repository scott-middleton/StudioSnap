package com.middleton.studiosnap.feature.processing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerateBatchPreviewsUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.processing.presentation.action.ProcessingUiAction
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingStatus
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProcessingViewModel(
    private val generationConfigHolder: GenerationConfigHolder,
    private val generationResultsHolder: GenerationResultsHolder,
    private val generateBatchPreviewsUseCase: GenerateBatchPreviewsUseCase,
    private val analyticsService: AnalyticsService
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
                // Transition to Generating before the API call starts
                _uiState.value = ProcessingUiState.Processing(
                    currentPhotoIndex = 0,
                    totalPhotos = currentConfig.photos.size,
                    styleName = currentConfig.style.displayName,
                    status = ProcessingStatus.Generating,
                    currentPhotoUri = firstPhotoUri
                )

                generateBatchPreviewsUseCase(currentConfig).collect { progress ->
                    // Photo just completed — show downloading stage briefly
                    _uiState.value = ProcessingUiState.Processing(
                        currentPhotoIndex = progress.currentIndex,
                        totalPhotos = progress.totalCount,
                        styleName = currentConfig.style.displayName,
                        status = ProcessingStatus.Downloading,
                        currentPhotoUri = currentConfig.photos.getOrNull(progress.currentIndex)?.localUri
                    )

                    if (progress.isComplete) {
                        generationResultsHolder.currentResults = progress.results
                        analyticsService.logEvent(
                            AnalyticsEvents.BATCH_GENERATION_COMPLETED,
                            mapOf(
                                "total" to progress.totalCount.toString(),
                                "success" to progress.successCount.toString(),
                                "failure" to progress.failureCount.toString()
                            )
                        )
                        _uiState.value = ProcessingUiState.Complete
                        _navigationEvent.value = ProcessingNavigationAction.GoToResults
                    } else {
                        // Move to next photo — show preparing then generating for new photo
                        val nextIndex = progress.currentIndex + 1
                        val nextPhotoUri = currentConfig.photos.getOrNull(nextIndex)?.localUri
                        _uiState.value = ProcessingUiState.Processing(
                            currentPhotoIndex = nextIndex,
                            totalPhotos = progress.totalCount,
                            styleName = currentConfig.style.displayName,
                            status = ProcessingStatus.Preparing,
                            currentPhotoUri = nextPhotoUri
                        )
                        _uiState.value = ProcessingUiState.Processing(
                            currentPhotoIndex = nextIndex,
                            totalPhotos = progress.totalCount,
                            styleName = currentConfig.style.displayName,
                            status = ProcessingStatus.Generating,
                            currentPhotoUri = nextPhotoUri
                        )
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
