package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.Style

/**
 * How the user chose the background for a generation batch. Picking preset styles and
 * typing a custom description are mutually exclusive — never combined. Selecting presets
 * clears any custom text and vice versa.
 *
 * [MultiPreset] holds 1–[HomeUiState.MAX_STYLES] preset styles (multi-select is only
 * offered when exactly one photo is selected; 2+ photos force a single preset). [Custom]
 * holds one free-text description applied to every selected photo.
 */
sealed interface BackgroundChoice {
    data class MultiPreset(val styles: List<Style>) : BackgroundChoice
    data class Custom(val description: String) : BackgroundChoice
}
