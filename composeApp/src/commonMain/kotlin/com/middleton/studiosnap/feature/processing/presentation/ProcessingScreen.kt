package com.middleton.studiosnap.feature.processing.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.processing.presentation.action.ProcessingUiAction
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import com.middleton.studiosnap.feature.processing.presentation.viewmodel.ProcessingViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.processing_cancel
import studiosnap.composeapp.generated.resources.processing_error_title
import studiosnap.composeapp.generated.resources.processing_go_back
import studiosnap.composeapp.generated.resources.processing_photo_progress
import studiosnap.composeapp.generated.resources.processing_please_wait
import studiosnap.composeapp.generated.resources.processing_retry
import studiosnap.composeapp.generated.resources.processing_title

@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<ProcessingNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(ProcessingUiAction.OnNavigationHandled)
        }
    }

    ProcessingScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun ProcessingScreenContent(
    state: ProcessingUiState,
    onAction: (ProcessingUiAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            ProcessingUiState.Loading -> LoadingContent()
            is ProcessingUiState.Processing -> ProcessingContent(
                currentPhotoIndex = state.currentPhotoIndex,
                totalPhotos = state.totalPhotos,
                overallProgress = state.overallProgress,
                onCancel = { onAction(ProcessingUiAction.OnCancelClicked) }
            )
            is ProcessingUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { onAction(ProcessingUiAction.OnRetryClicked) },
                onGoBack = { onAction(ProcessingUiAction.OnCancelClicked) }
            )
            ProcessingUiState.Complete -> {
                // Brief transitional state — navigation fires immediately
                LoadingContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.processing_title),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ProcessingContent(
    currentPhotoIndex: Int,
    totalPhotos: Int,
    overallProgress: Float,
    onCancel: () -> Unit
) {
    val progressText = if (totalPhotos == 1) {
        stringResource(Res.string.processing_title)
    } else {
        stringResource(Res.string.processing_photo_progress, currentPhotoIndex + 1, totalPhotos)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            fontSize = MaterialTheme.typography.displayLarge.fontSize,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(Res.string.processing_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = progressText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { overallProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(overallProgress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.processing_please_wait),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onCancel) {
            Text(stringResource(Res.string.processing_cancel))
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = MaterialTheme.typography.displayMedium.fontSize
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.processing_error_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text(stringResource(Res.string.processing_retry))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onGoBack) {
            Text(stringResource(Res.string.processing_go_back))
        }
    }
}
