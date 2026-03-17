package com.middleton.studiosnap.feature.results.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

data class ResultsUiState(
    val results: List<ResultItem> = emptyList(),
    val snackbarMessage: UiText? = null
) {
    val successCount: Int
        get() = results.count { it.result is GenerationResult.Success }
}

data class ResultItem(
    val result: GenerationResult,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showingOriginal: Boolean = false
)
