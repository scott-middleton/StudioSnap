package com.middleton.studiosnap.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryFilter
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryItem
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<HistoryNavigationAction?>(null)
    val navigationEvent: StateFlow<HistoryNavigationAction?> = _navigationEvent.asStateFlow()

    private var allItems: List<HistoryItem> = emptyList()

    init {
        observeHistory()
    }

    fun handleAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.OnItemClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoToResultDetail(action.itemId)
            is HistoryUiAction.OnDeleteClicked -> deleteItem(action.itemId)
            is HistoryUiAction.OnFilterChanged -> changeFilter(action.filter)
            HistoryUiAction.OnBackClicked ->
                _navigationEvent.value = HistoryNavigationAction.GoBack
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.getAll().collect { results ->
                allItems = results.map { result ->
                    HistoryItem(
                        id = result.generationId,
                        inputPhotoUri = result.inputPhoto.localUri,
                        watermarkedUri = result.watermarkedPreviewUri,
                        fullResLocalUri = result.fullResUri,
                        styleName = result.styleName,
                        isPurchased = result.fullResUri != null,
                        createdAt = result.createdAt,
                        imageWidth = result.imageWidth,
                        imageHeight = result.imageHeight
                    )
                }
                applyFilter()
            }
        }
    }

    private fun changeFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
        applyFilter()
    }

    private fun applyFilter() {
        val filter = _uiState.value.filter
        val filtered = when (filter) {
            HistoryFilter.ALL -> allItems
            HistoryFilter.PURCHASED -> allItems.filter { it.isPurchased }
            HistoryFilter.PREVIEWS -> allItems.filter { !it.isPurchased }
        }
        _uiState.update { it.copy(items = filtered, isLoading = false) }
    }

    private fun deleteItem(itemId: String) {
        viewModelScope.launch {
            historyRepository.delete(itemId)
        }
    }
}
