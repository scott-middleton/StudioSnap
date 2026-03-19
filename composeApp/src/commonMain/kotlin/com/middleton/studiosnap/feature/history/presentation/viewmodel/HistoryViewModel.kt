package com.middleton.studiosnap.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
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
        observeHistory()
        analyticsService.logEvent(AnalyticsEvents.HISTORY_VIEWED)
    }

    fun handleAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.OnItemClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoToResultDetail(action.itemId)
            is HistoryUiAction.OnDeleteClicked -> deleteItem(action.itemId)
            HistoryUiAction.OnBackClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoBack
            HistoryUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.getAll().collect { results ->
                val items = results.map { result ->
                    HistoryItem(
                        id = result.generationId,
                        previewUri = result.previewUri,
                        styleName = result.styleDisplayName,
                        createdAt = result.createdAt
                    )
                }
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    private fun deleteItem(itemId: String) {
        viewModelScope.launch {
            historyRepository.delete(itemId)
        }
    }
}
