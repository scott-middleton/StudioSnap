package com.middleton.studiosnap.feature.settings.presentation.ui_state

data class SettingsUiState(
    val creditBalance: Int = 0,
    val isSignedIn: Boolean = false,
    val appVersion: String = "",
    val showSignOutConfirmation: Boolean = false,
    val showDeleteAccountConfirmation: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null
)
