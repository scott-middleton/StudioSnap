package com.middleton.studiosnap.feature.settings.presentation.action

import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality

sealed interface SettingsUiAction {
    data object OnBuyCreditsClicked : SettingsUiAction
    data object OnPrivacyPolicyClicked : SettingsUiAction
    data object OnTermsClicked : SettingsUiAction
    data object OnBackClicked : SettingsUiAction
    data object OnNavigationHandled : SettingsUiAction
    data class OnQualityChanged(val quality: GenerationQuality) : SettingsUiAction
}
