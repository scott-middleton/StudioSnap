package com.middleton.studiosnap.feature.home.domain.model

import com.middleton.studiosnap.core.domain.model.UiText
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.DrawableResource

/**
 * A visual style preset. Users select styles from a grid — each maps to a hidden Kontext prompt.
 * Styles are categorised for filtering and optionally seasonal.
 *
 * To add a new style:
 * 1. Add a 512x512 WebP thumbnail to composeResources/drawable/
 * 2. Add a string resource for the display name
 * 3. Add a new Style entry to StyleRepositoryImpl — that's it, no mapping functions to update.
 */
data class Style(
    val id: String,
    val displayName: UiText,
    val categories: Set<StyleCategory>,
    val thumbnail: DrawableResource?,
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
