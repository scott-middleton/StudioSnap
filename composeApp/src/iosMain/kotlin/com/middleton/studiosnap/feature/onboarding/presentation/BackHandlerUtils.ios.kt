package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a system back button, so this is a no-op
}