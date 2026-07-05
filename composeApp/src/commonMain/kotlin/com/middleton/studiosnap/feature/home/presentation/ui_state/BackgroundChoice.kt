package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.Style

/**
 * Picking a preset style and typing a custom description are mutually
 * exclusive ways of choosing a generation's background — never combined.
 */
sealed interface BackgroundChoice {
    data class Preset(val style: Style) : BackgroundChoice
    data class Custom(val description: String) : BackgroundChoice
}
