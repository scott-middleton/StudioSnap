package com.middleton.studiosnap.feature.settings.presentation.action

sealed interface SettingsUiAction {
    data object OnBuyCreditsClicked : SettingsUiAction
    data object OnPrivacyPolicyClicked : SettingsUiAction
    data object OnTermsClicked : SettingsUiAction
    data object OnBackClicked : SettingsUiAction
    data object OnNavigationHandled : SettingsUiAction
}
