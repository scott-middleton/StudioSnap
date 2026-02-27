package com.middleton.studiosnap.feature.results.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

data class ResultsUiState(
    val results: List<ResultItem> = emptyList(),
    val creditBalance: Int = 0,
    val snackbarMessage: String? = null
) {
    val successCount: Int
        get() = results.count { it.result is GenerationResult.Success }

    val downloadableCount: Int
        get() = results.count { it.result is GenerationResult.Success && !it.isPurchased }
}

data class ResultItem(
    val result: GenerationResult,
    val isPurchased: Boolean = false,
    val isDownloading: Boolean = false,
    val fullResLocalUri: String? = null
)
