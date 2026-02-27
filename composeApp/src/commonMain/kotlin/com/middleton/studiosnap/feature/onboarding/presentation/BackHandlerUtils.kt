package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)