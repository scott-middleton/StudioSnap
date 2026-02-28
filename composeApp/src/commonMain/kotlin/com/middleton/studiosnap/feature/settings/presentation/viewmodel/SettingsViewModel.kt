package com.middleton.studiosnap.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.feature.settings.presentation.ui_state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val creditQueries: CreditQueries,
    private val authService: AuthService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SettingsNavigationAction?>(null)
    val navigationEvent: StateFlow<SettingsNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        loadSettings()
        observeCredits()
        analyticsService.logEvent(AnalyticsEvents.SETTINGS_VIEWED)
    }

    fun handleAction(action: SettingsUiAction) {
        when (action) {
            SettingsUiAction.OnBuyCreditsClicked ->
                _navigationEvent.value = SettingsNavigationAction.GoToCreditStore
            SettingsUiAction.OnBackClicked ->
                _navigationEvent.value = SettingsNavigationAction.GoBack
            SettingsUiAction.OnPrivacyPolicyClicked -> { /* Handled by screen — opens URL */ }
            SettingsUiAction.OnTermsClicked -> { /* Handled by screen — opens URL */ }
            SettingsUiAction.OnNavigationHandled -> _navigationEvent.value = null
            is SettingsUiAction.OnQualityChanged -> updateQuality(action.quality)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val qualityString = userPreferencesRepository.getPreferredQuality()
            val quality = GenerationQuality.entries.find { it.name == qualityString }
                ?: GenerationQuality.DEFAULT
            val isSignedIn = authService.isSignedIn.value
            _uiState.update {
                it.copy(
                    preferredQuality = quality,
                    isSignedIn = isSignedIn
                )
            }
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            creditQueries.observeCredits().collect { credits ->
                _uiState.update { it.copy(creditBalance = credits.amount) }
            }
        }
    }

    private fun updateQuality(quality: GenerationQuality) {
        _uiState.update { it.copy(preferredQuality = quality) }
        viewModelScope.launch {
            userPreferencesRepository.setPreferredQuality(quality.name)
        }
    }
}
