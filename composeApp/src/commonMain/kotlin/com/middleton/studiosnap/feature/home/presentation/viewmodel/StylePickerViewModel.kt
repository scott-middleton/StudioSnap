package com.middleton.studiosnap.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.StylePickerUiAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.StylePickerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StylePickerViewModel(
    private val styleRepository: StyleRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StylePickerUiState())
    val uiState: StateFlow<StylePickerUiState> = _uiState.asStateFlow()

    init {
        loadStyles()
    }

    fun handleAction(action: StylePickerUiAction) {
        when (action) {
            is StylePickerUiAction.OnCategorySelected -> selectCategory(action.category)
        }
    }

    private fun loadStyles() {
        viewModelScope.launch {
            val lastCategory = userPreferencesRepository.getLastUsedCategoryFilter()
            val category = StyleCategory.entries.find { it.name == lastCategory } ?: StyleCategory.ALL
            val styles = if (category == StyleCategory.ALL) {
                styleRepository.getAllStyles()
            } else {
                styleRepository.getStylesByCategory(category)
            }
            _uiState.update {
                it.copy(styles = styles, selectedCategory = category)
            }
        }
    }

    private fun selectCategory(category: StyleCategory) {
        val styles = if (category == StyleCategory.ALL) {
            styleRepository.getAllStyles()
        } else {
            styleRepository.getStylesByCategory(category)
        }
        _uiState.update { it.copy(selectedCategory = category, styles = styles) }

        viewModelScope.launch {
            userPreferencesRepository.setLastUsedCategoryFilter(category.name)
        }
    }
}
