package com.middleton.studiosnap.feature.sessiondetail.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

sealed interface SessionDetailUiState {
    data object Loading : SessionDetailUiState
    data class Success(
        val sessionId: String,
        val displayLabel: String,
        val results: List<GenerationResult.Success>,
        val showDeleteConfirm: Boolean = false
    ) : SessionDetailUiState
    data object Error : SessionDetailUiState
}
