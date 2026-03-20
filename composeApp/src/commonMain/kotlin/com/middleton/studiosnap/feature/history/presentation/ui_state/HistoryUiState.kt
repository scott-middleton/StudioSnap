package com.middleton.studiosnap.feature.history.presentation.ui_state

import com.middleton.studiosnap.feature.history.domain.model.HistorySession

data class HistoryUiState(
    val sessions: List<HistorySession> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = sessions.isEmpty() && !isLoading
}
