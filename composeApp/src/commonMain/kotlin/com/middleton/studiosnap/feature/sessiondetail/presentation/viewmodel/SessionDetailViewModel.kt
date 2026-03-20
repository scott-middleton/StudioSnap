package com.middleton.studiosnap.feature.sessiondetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.sessiondetail.presentation.action.SessionDetailUiAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.navigation.SessionDetailNavigationAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.ui_state.SessionDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val sessionId: String,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionDetailUiState>(SessionDetailUiState.Loading)
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SessionDetailNavigationAction?>(null)
    val navigationEvent: StateFlow<SessionDetailNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        observeSession()
    }

    fun handleAction(action: SessionDetailUiAction) {
        when (action) {
            SessionDetailUiAction.OnBackClicked ->
                _navigationEvent.value = SessionDetailNavigationAction.GoBack
            SessionDetailUiAction.OnNavigationHandled ->
                _navigationEvent.value = null
            SessionDetailUiAction.OnOpenInGalleryClicked ->
                openInGallery()
            is SessionDetailUiAction.OnDeleteSessionClicked ->
                showDeleteConfirm()
            SessionDetailUiAction.OnDeleteConfirmed ->
                confirmDelete()
            SessionDetailUiAction.OnDeleteDismissed ->
                dismissDelete()
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            combine(
                historyRepository.getSessions(),
                historyRepository.getBySessionId(sessionId)
            ) { sessions, results ->
                val session = sessions.find { it.batchId == sessionId }
                    ?: return@combine SessionDetailUiState.Error
                val showDelete = (_uiState.value as? SessionDetailUiState.Success)?.showDeleteConfirm ?: false
                SessionDetailUiState.Success(
                    sessionId = sessionId,
                    displayLabel = session.displayLabel,
                    results = results,
                    showDeleteConfirm = showDelete
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun openInGallery() {
        viewModelScope.launch {
            com.middleton.studiosnap.core.presentation.util.openInGallery("")
        }
    }

    private fun showDeleteConfirm() {
        _uiState.update { current ->
            (current as? SessionDetailUiState.Success)?.copy(showDeleteConfirm = true) ?: current
        }
    }

    private fun dismissDelete() {
        _uiState.update { current ->
            (current as? SessionDetailUiState.Success)?.copy(showDeleteConfirm = false) ?: current
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            historyRepository.deleteSession(sessionId)
            _navigationEvent.value = SessionDetailNavigationAction.GoBack
        }
    }
}
