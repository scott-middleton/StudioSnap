package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.theme.studioSnapTextStyles
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.onboarding_demo_headline
import studiosnap.composeapp.generated.resources.onboarding_demo_tagline

/**
 * Stub — will be replaced with StudioSnap-specific demo content
 * showing product photo style transformations.
 */
@Composable
fun OnboardingDemoPage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.onboarding_demo_headline),
            style = studioSnapTextStyles.onboardingHeadline,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.onboarding_demo_tagline),
            style = studioSnapTextStyles.onboardingSubheadline,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
