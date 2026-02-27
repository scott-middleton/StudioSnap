package com.middleton.studiosnap.core.presentation.util

import androidx.compose.runtime.Composable

// iOS manages status bar appearance via UIViewController — no manual override needed.
@Composable
actual fun SystemBarsAppearance(lightIcons: Boolean) = Unit
