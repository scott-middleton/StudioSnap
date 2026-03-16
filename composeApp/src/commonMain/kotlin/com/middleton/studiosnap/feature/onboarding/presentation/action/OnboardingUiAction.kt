package com.middleton.studiosnap.feature.onboarding.presentation.action

sealed interface OnboardingUiAction {
    data object NextPage : OnboardingUiAction
    data class NavigateToPage(val page: Int) : OnboardingUiAction
    data object GetStarted : OnboardingUiAction
    data object TriggerValuePageAnimation : OnboardingUiAction
    data object OnNavigationHandled : OnboardingUiAction
}
