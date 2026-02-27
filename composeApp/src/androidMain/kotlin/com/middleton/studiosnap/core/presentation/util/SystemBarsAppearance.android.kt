package com.middleton.studiosnap.core.presentation.util

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun SystemBarsAppearance(lightIcons: Boolean) {
    val activity = LocalActivity.current ?: return
    DisposableEffect(lightIcons) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val prevLightStatus = controller.isAppearanceLightStatusBars
        val prevLightNav = controller.isAppearanceLightNavigationBars

        controller.isAppearanceLightStatusBars = !lightIcons
        controller.isAppearanceLightNavigationBars = !lightIcons

        onDispose {
            controller.isAppearanceLightStatusBars = prevLightStatus
            controller.isAppearanceLightNavigationBars = prevLightNav
        }
    }
}
