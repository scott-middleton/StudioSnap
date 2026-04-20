package com.middleton.studiosnap.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.domain.service.RatingService
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.feature.settings.presentation.ui_state.SettingsUiState
import com.middleton.studiosnap.purchases.PurchasesIdentifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val creditQueries: CreditQueries,
    private val authService: AuthService,
    private val analyticsService: AnalyticsService,
    private val ratingService: RatingService,
    private val purchasesIdentifier: PurchasesIdentifier
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
            SettingsUiAction.OnSupportClicked -> { /* Handled by screen — opens URL */ }
            SettingsUiAction.OnNavigationHandled -> _navigationEvent.value = null

            SettingsUiAction.OnSignOutClicked ->
                _uiState.update { it.copy(showSignOutConfirmation = true) }
            SettingsUiAction.OnSignOutDismissed ->
                _uiState.update { it.copy(showSignOutConfirmation = false) }
            SettingsUiAction.OnSignOutConfirmed -> signOut()

            SettingsUiAction.OnDeleteAccountClicked ->
                _uiState.update { it.copy(showDeleteAccountConfirmation = true) }
            SettingsUiAction.OnDeleteAccountDismissed ->
                _uiState.update { it.copy(showDeleteAccountConfirmation = false) }
            SettingsUiAction.OnDeleteAccountConfirmed -> deleteAccount()
            SettingsUiAction.OnDeleteAccountErrorDismissed ->
                _uiState.update { it.copy(deleteAccountError = null) }

            SettingsUiAction.OnRateAppClicked -> {
                viewModelScope.launch { ratingService.openStoreReviewPage() }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(showSignOutConfirmation = false) }
            authService.signOut()
            purchasesIdentifier.clearIdentity()
            _navigationEvent.value = SettingsNavigationAction.GoToSplashAfterSignOut
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(showDeleteAccountConfirmation = false, isDeletingAccount = true)
            }
            authService.deleteAccount()
                .onSuccess {
                    purchasesIdentifier.clearIdentity()
                    _uiState.update { it.copy(isDeletingAccount = false) }
                    _navigationEvent.value = SettingsNavigationAction.GoToSplashAfterSignOut
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            deleteAccountError = error.message ?: "Failed to delete account"
                        )
                    }
                }
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

    companion object {
        const val PRIVACY_POLICY_URL = "https://scott-middleton.github.io/studiosnap/privacy-policy.html"
        const val SUPPORT_URL = "https://scott-middleton.github.io/studiosnap/support.html"
    }
}
