package com.middleton.studiosnap.feature.settings.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality

data class SettingsUiState(
    val creditBalance: Int = 0,
    val isSignedIn: Boolean = false,
    val preferredQuality: GenerationQuality = GenerationQuality.DEFAULT,
    val appVersion: String = ""
)
