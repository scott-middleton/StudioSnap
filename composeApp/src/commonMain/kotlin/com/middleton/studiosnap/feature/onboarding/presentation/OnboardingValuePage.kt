package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.get_started_button
import studiosnap.composeapp.generated.resources.onboarding_value_headline

/**
 * Stub — will be fully reimplemented for StudioSnap value proposition page.
 */
@Composable
fun OnboardingValuePage(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Res.string.onboarding_value_headline))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGetStarted) {
            Text(stringResource(Res.string.get_started_button))
        }
    }
}
