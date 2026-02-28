package com.middleton.studiosnap.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.feature.settings.presentation.ui_state.SettingsUiState
import com.middleton.studiosnap.feature.settings.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.settings_about
import studiosnap.composeapp.generated.resources.settings_credit_count
import studiosnap.composeapp.generated.resources.settings_credits
import studiosnap.composeapp.generated.resources.settings_get_more_credits
import studiosnap.composeapp.generated.resources.settings_privacy_policy
import studiosnap.composeapp.generated.resources.settings_quality
import studiosnap.composeapp.generated.resources.settings_quality_high
import studiosnap.composeapp.generated.resources.settings_quality_standard
import studiosnap.composeapp.generated.resources.settings_title

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<SettingsNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(SettingsUiAction.OnNavigationHandled)
        }
    }

    SettingsScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onAction: (SettingsUiAction) -> Unit
) {
    Scaffold(
        topBar = {
            StudioSnapTopBar(
                title = stringResource(Res.string.settings_title),
                onBack = { onAction(SettingsUiAction.OnBackClicked) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Credits section
            SectionHeader(stringResource(Res.string.settings_credits))

            SettingsRow(
                label = stringResource(Res.string.settings_credit_count, state.creditBalance),
                subtitle = stringResource(Res.string.settings_get_more_credits),
                onClick = { onAction(SettingsUiAction.OnBuyCreditsClicked) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Quality section
            SectionHeader(stringResource(Res.string.settings_quality))

            QualityOption(
                label = stringResource(Res.string.settings_quality_standard),
                selected = state.preferredQuality == GenerationQuality.STANDARD,
                onClick = { onAction(SettingsUiAction.OnQualityChanged(GenerationQuality.STANDARD)) }
            )

            QualityOption(
                label = stringResource(Res.string.settings_quality_high),
                selected = state.preferredQuality == GenerationQuality.HIGH,
                onClick = { onAction(SettingsUiAction.OnQualityChanged(GenerationQuality.HIGH)) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // About section
            SectionHeader(stringResource(Res.string.settings_about))

            SettingsRow(
                label = stringResource(Res.string.settings_privacy_policy),
                onClick = { onAction(SettingsUiAction.OnPrivacyPolicyClicked) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QualityOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
