package com.middleton.studiosnap.feature.home.domain.model

import kotlinx.datetime.Month

/**
 * A visual style preset. Users select styles from a grid — each maps to a hidden Kontext prompt.
 * Styles are categorised for filtering and optionally seasonal.
 */
data class Style(
    val id: String,
    val nameKey: String,
    val categories: Set<StyleCategory>,
    val thumbnailResName: String,
    val kontextPrompt: String,
    val seasonal: SeasonalWindow? = null
)

/**
 * Defines when a seasonal style should be promoted (sorted to top).
 * Style remains accessible outside the window — just not featured.
 */
data class SeasonalWindow(
    val startMonth: Month,
    val endMonth: Month
)
