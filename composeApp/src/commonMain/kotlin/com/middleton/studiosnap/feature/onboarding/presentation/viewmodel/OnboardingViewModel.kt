package com.middleton.studiosnap.feature.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.navigation.OnboardingNavigationAction
import com.middleton.studiosnap.feature.onboarding.presentation.ui_state.OnboardingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<OnboardingNavigationAction?>(null)
    val navigationEvent: StateFlow<OnboardingNavigationAction?> = _navigationEvent.asStateFlow()

    fun handleAction(action: OnboardingUiAction) {
        when (action) {
            is OnboardingUiAction.NextPage -> {
                _uiState.update {
                    val nextPage = it.currentPage + 1
                    if (nextPage < TOTAL_PAGES) {
                        it.copy(currentPage = nextPage)
                    } else {
                        it
                    }
                }
            }

            is OnboardingUiAction.NavigateToPage -> {
                if (action.page in 0 until TOTAL_PAGES) {
                    _uiState.update { it.copy(currentPage = action.page) }
                }
            }

            OnboardingUiAction.TriggerValuePageAnimation -> {
                _uiState.update { it.copy(valuePageAnimationTrigger = it.valuePageAnimationTrigger + 1) }
            }

            OnboardingUiAction.GetStarted -> {
                analyticsService.logEvent(AnalyticsEvents.ONBOARDING_COMPLETED)
                viewModelScope.launch {
                    userPreferencesRepository.setHasCompletedOnboarding()
                    _navigationEvent.value = OnboardingNavigationAction.GoToHome
                }
            }

            OnboardingUiAction.OnNavigationHandled -> _navigationEvent.value = null
        }
    }

    companion object {
        const val TOTAL_PAGES = 4
    }
}
