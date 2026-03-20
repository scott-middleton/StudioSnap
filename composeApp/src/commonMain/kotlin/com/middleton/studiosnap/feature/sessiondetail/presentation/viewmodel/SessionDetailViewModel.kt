package com.middleton.studiosnap.feature.sessiondetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.presentation.util.openInGallery
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
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

    /** Separate flow so combine() picks up confirm-dialog toggles without a race. */
    private val _showDeleteConfirm = MutableStateFlow(false)

    init {
        observeSession()
    }

    fun handleAction(action: SessionDetailUiAction) {
        when (action) {
            SessionDetailUiAction.OnBackClicked ->
                _navigationEvent.update { SessionDetailNavigationAction.GoBack }
            SessionDetailUiAction.OnNavigationHandled ->
                _navigationEvent.update { null }
            SessionDetailUiAction.OnOpenInGalleryClicked ->
                openInGallery()
            is SessionDetailUiAction.OnDeleteSessionClicked ->
                _showDeleteConfirm.update { true }
            SessionDetailUiAction.OnDeleteConfirmed ->
                confirmDelete()
            SessionDetailUiAction.OnDeleteDismissed ->
                _showDeleteConfirm.update { false }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            combine(
                historyRepository.getSessions(),
                historyRepository.getBySessionId(sessionId),
                _showDeleteConfirm
            ) { sessions, results, showDelete ->
                val session = sessions.find { it.batchId == sessionId }
                    ?: return@combine SessionDetailUiState.Error
                SessionDetailUiState.Success(
                    sessionId = sessionId,
                    displayLabel = session.displayLabel,
                    results = results,
                    showDeleteConfirm = showDelete
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private fun openInGallery() {
        val results = (_uiState.value as? SessionDetailUiState.Success)?.results
            ?: return
        val firstUri = results.filterIsInstance<GenerationResult.Success>()
            .firstOrNull()?.previewUri ?: return
        viewModelScope.launch {
            openInGallery(firstUri)
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            historyRepository.deleteSession(sessionId)
            _navigationEvent.update { SessionDetailNavigationAction.GoBack }
        }
    }
}
