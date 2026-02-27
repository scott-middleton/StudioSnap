package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Stub — will be replaced with StudioSnap-specific demo content
 * showing product photo style transformations.
 * TODO: Replace hardcoded strings with Res.string resources in Phase 5.
 * TODO: Render subheadline or remove parameter when implementing real UI.
 */
@Composable
fun OnboardingDemoPage(
    headline: String,
    subheadline: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = headline)
    }
}
