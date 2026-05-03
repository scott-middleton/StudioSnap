package com.middleton.studiosnap.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.feature.settings.presentation.ui_state.SettingsUiState
import com.middleton.studiosnap.feature.settings.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.settings_about
import studiosnap.composeapp.generated.resources.settings_account
import studiosnap.composeapp.generated.resources.settings_cancel
import studiosnap.composeapp.generated.resources.settings_credit_count
import studiosnap.composeapp.generated.resources.settings_credits
import studiosnap.composeapp.generated.resources.settings_delete_account
import studiosnap.composeapp.generated.resources.settings_delete_account_confirm
import studiosnap.composeapp.generated.resources.settings_delete_account_message
import studiosnap.composeapp.generated.resources.settings_delete_account_title
import studiosnap.composeapp.generated.resources.settings_delete_account_success_title
import studiosnap.composeapp.generated.resources.settings_delete_account_success_message
import studiosnap.composeapp.generated.resources.settings_get_more_credits
import studiosnap.composeapp.generated.resources.settings_privacy_policy
import studiosnap.composeapp.generated.resources.settings_rate_app
import studiosnap.composeapp.generated.resources.settings_sign_out
import studiosnap.composeapp.generated.resources.settings_sign_out_confirm
import studiosnap.composeapp.generated.resources.settings_sign_out_message
import studiosnap.composeapp.generated.resources.settings_sign_out_title
import studiosnap.composeapp.generated.resources.settings_support
import studiosnap.composeapp.generated.resources.settings_ok
import studiosnap.composeapp.generated.resources.settings_title

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<SettingsNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(SettingsUiAction.OnNavigationHandled)
        }
    }

    SettingsScreenContent(
        state = uiState,
        onAction = viewModel::handleAction,
        onOpenUrl = { url -> uriHandler.openUri(url) }
    )
}

@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onAction: (SettingsUiAction) -> Unit,
    onOpenUrl: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                // Account section (only when signed in)
                if (state.isSignedIn) {
                    SectionHeader(stringResource(Res.string.settings_account))

                    SettingsRow(
                        label = stringResource(Res.string.settings_credit_count, state.creditBalance),
                        subtitle = stringResource(Res.string.settings_get_more_credits),
                        onClick = { onAction(SettingsUiAction.OnBuyCreditsClicked) }
                    )

                    SettingsRow(
                        label = stringResource(Res.string.settings_sign_out),
                        onClick = { onAction(SettingsUiAction.OnSignOutClicked) }
                    )

                    SettingsRow(
                        label = stringResource(Res.string.settings_delete_account),
                        labelColor = MaterialTheme.colorScheme.error,
                        onClick = { onAction(SettingsUiAction.OnDeleteAccountClicked) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                } else {
                    // Credits section (when not signed in)
                    SectionHeader(stringResource(Res.string.settings_credits))

                    SettingsRow(
                        label = stringResource(Res.string.settings_credit_count, state.creditBalance),
                        subtitle = stringResource(Res.string.settings_get_more_credits),
                        onClick = { onAction(SettingsUiAction.OnBuyCreditsClicked) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // About section
                SectionHeader(stringResource(Res.string.settings_about))

                SettingsRow(
                    label = stringResource(Res.string.settings_privacy_policy),
                    onClick = {
                        onOpenUrl(SettingsViewModel.PRIVACY_POLICY_URL)
                    }
                )

                SettingsRow(
                    label = stringResource(Res.string.settings_support),
                    onClick = {
                        onOpenUrl(SettingsViewModel.SUPPORT_URL)
                    }
                )

                SettingsRow(
                    label = stringResource(Res.string.settings_rate_app),
                    onClick = { onAction(SettingsUiAction.OnRateAppClicked) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Sign out confirmation dialog
        if (state.showSignOutConfirmation) {
            SignOutConfirmationDialog(
                onConfirm = { onAction(SettingsUiAction.OnSignOutConfirmed) },
                onDismiss = { onAction(SettingsUiAction.OnSignOutDismissed) }
            )
        }

        // Delete account confirmation dialog
        if (state.showDeleteAccountConfirmation) {
            DeleteAccountConfirmationDialog(
                onConfirm = { onAction(SettingsUiAction.OnDeleteAccountConfirmed) },
                onDismiss = { onAction(SettingsUiAction.OnDeleteAccountDismissed) }
            )
        }

        // Sign out error dialog
        state.signOutError?.let { error ->
            AlertDialog(
                onDismissRequest = { onAction(SettingsUiAction.OnSignOutErrorDismissed) },
                title = { Text(stringResource(Res.string.settings_sign_out_title)) },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { onAction(SettingsUiAction.OnSignOutErrorDismissed) }) {
                        Text(stringResource(Res.string.settings_ok))
                    }
                }
            )
        }

        // Delete account success dialog
        if (state.showDeleteAccountSuccess) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(Res.string.settings_delete_account_success_title)) },
                text = { Text(stringResource(Res.string.settings_delete_account_success_message)) },
                confirmButton = {
                    TextButton(onClick = { onAction(SettingsUiAction.OnDeleteAccountSuccessDismissed) }) {
                        Text(stringResource(Res.string.settings_ok))
                    }
                }
            )
        }

        // Delete account error dialog
        state.deleteAccountError?.let { error ->
            AlertDialog(
                onDismissRequest = { onAction(SettingsUiAction.OnDeleteAccountErrorDismissed) },
                title = { Text(stringResource(Res.string.settings_delete_account_title)) },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { onAction(SettingsUiAction.OnDeleteAccountErrorDismissed) }) {
                        Text(stringResource(Res.string.settings_ok))
                    }
                }
            )
        }

        // Loading overlay for sign out or delete account
        if (state.isSigningOut || state.isDeletingAccount) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
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
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor
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
private fun SignOutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_sign_out_title)) },
        text = { Text(stringResource(Res.string.settings_sign_out_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.settings_sign_out_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_cancel))
            }
        }
    )
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_delete_account_title)) },
        text = { Text(stringResource(Res.string.settings_delete_account_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(Res.string.settings_delete_account_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_cancel))
            }
        }
    )
}

