package com.middleton.studiosnap.feature.results.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

data class ResultsUiState(
    val results: List<ResultItem> = emptyList(),
    val isAutoSaving: Boolean = false,
    val snackbarMessage: UiText? = null
)

data class ResultItem(
    val result: GenerationResult,
    val isSavedToGallery: Boolean = false,
    val showingOriginal: Boolean = false
)
