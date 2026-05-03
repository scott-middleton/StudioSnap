package com.middleton.studiosnap.feature.settings.presentation.action

sealed interface SettingsUiAction {
    data object OnBuyCreditsClicked : SettingsUiAction
    data object OnTermsClicked : SettingsUiAction
    data object OnBackClicked : SettingsUiAction
    data object OnNavigationHandled : SettingsUiAction

    data object OnSignOutClicked : SettingsUiAction
    data object OnSignOutConfirmed : SettingsUiAction
    data object OnSignOutDismissed : SettingsUiAction
    data object OnSignOutErrorDismissed : SettingsUiAction
    data object OnDeleteAccountClicked : SettingsUiAction
    data object OnDeleteAccountConfirmed : SettingsUiAction
    data object OnDeleteAccountDismissed : SettingsUiAction
    data object OnDeleteAccountErrorDismissed : SettingsUiAction
    data object OnDeleteAccountSuccessDismissed : SettingsUiAction
    data object OnRateAppClicked : SettingsUiAction
}
