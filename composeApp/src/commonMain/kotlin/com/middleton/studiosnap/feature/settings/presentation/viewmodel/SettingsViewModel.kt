package com.middleton.studiosnap.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
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
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isSignedIn = authService.isSignedIn.value
            _uiState.update { it.copy(isSignedIn = isSignedIn) }
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            creditQueries.observeCredits().collect { credits ->
                _uiState.update { it.copy(creditBalance = credits.amount) }
            }
        }
    }
}
