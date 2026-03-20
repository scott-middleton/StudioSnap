package com.middleton.studiosnap.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<HistoryNavigationAction?>(null)
    val navigationEvent: StateFlow<HistoryNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        observeSessions()
        analyticsService.logEvent(AnalyticsEvents.HISTORY_VIEWED)
    }

    fun handleAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.OnSessionClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoToSessionDetail(action.sessionId)
            is HistoryUiAction.OnDeleteSessionClicked -> deleteSession(action.sessionId)
            HistoryUiAction.OnBackClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoBack
            HistoryUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            historyRepository.getSessions().collect { sessions ->
                _uiState.update { it.copy(sessions = sessions, isLoading = false) }
            }
        }
    }

    private fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            historyRepository.deleteSession(sessionId)
        }
    }
}
