package com.middleton.studiosnap.core.presentation.util

import androidx.compose.runtime.Composable

/**
 * Controls the appearance of system bar icons (status bar + navigation bar).
 * Call on screens with dark backgrounds to ensure icons are light/visible.
 * Restores the previous appearance when the composable leaves the composition.
 *
 * @param lightIcons true = light icons (for dark backgrounds), false = dark icons (for light backgrounds)
 */
@Composable
expect fun SystemBarsAppearance(lightIcons: Boolean)
