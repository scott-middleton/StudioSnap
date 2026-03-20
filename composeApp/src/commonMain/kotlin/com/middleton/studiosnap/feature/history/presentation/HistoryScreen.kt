package com.middleton.studiosnap.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.RestorationImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.util.formatRelativeTime
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryUiState
import com.middleton.studiosnap.feature.history.presentation.viewmodel.HistoryViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.history_delete
import studiosnap.composeapp.generated.resources.history_delete_cancel
import studiosnap.composeapp.generated.resources.history_delete_confirm
import studiosnap.composeapp.generated.resources.history_delete_message
import studiosnap.composeapp.generated.resources.history_delete_title
import studiosnap.composeapp.generated.resources.history_empty_subtitle
import studiosnap.composeapp.generated.resources.history_empty_title
import studiosnap.composeapp.generated.resources.history_product_photo
import studiosnap.composeapp.generated.resources.history_photo_count
import studiosnap.composeapp.generated.resources.history_photo_count_one
import studiosnap.composeapp.generated.resources.history_title

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<HistoryNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(HistoryUiAction.OnNavigationHandled)
        }
    }

    HistoryScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun HistoryScreenContent(
    state: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit
) {
    var deleteDialogSessionId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            StudioSnapTopBar(
                title = stringResource(Res.string.history_title),
                onBack = { onAction(HistoryUiAction.OnBackClicked) }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.isEmpty -> {
                EmptyHistoryContent(modifier = Modifier.padding(padding))
            }
            else -> {
                SessionList(
                    sessions = state.sessions,
                    modifier = Modifier.padding(padding),
                    onSessionClicked = { onAction(HistoryUiAction.OnSessionClicked(it)) },
                    onDeleteClicked = { deleteDialogSessionId = it }
                )
            }
        }
    }

    deleteDialogSessionId?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { deleteDialogSessionId = null },
            title = { Text(stringResource(Res.string.history_delete_title)) },
            text = { Text(stringResource(Res.string.history_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(HistoryUiAction.OnDeleteSessionClicked(sessionId))
                    deleteDialogSessionId = null
                }) {
                    Text(stringResource(Res.string.history_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogSessionId = null }) {
                    Text(stringResource(Res.string.history_delete_cancel))
                }
            }
        )
    }
}

// ─── Empty State ────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ImageSearch,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.history_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Session List ────────────────────────────────────────────────────────────

@Composable
private fun SessionList(
    sessions: List<HistorySession>,
    modifier: Modifier = Modifier,
    onSessionClicked: (String) -> Unit,
    onDeleteClicked: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        items(sessions, key = { it.batchId }) { session ->
            SessionCard(
                session = session,
                onClick = { onSessionClicked(session.batchId) },
                onDelete = { onDeleteClicked(session.batchId) }
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SessionCard(
    session: HistorySession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    StudioSnapCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Thumbnail strip
            if (session.thumbnailUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(session.thumbnailUris) { uri ->
                        RestorationImage(
                            imagePath = uri,
                            contentDescription = stringResource(Res.string.history_product_photo),
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.displayLabel,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val photoCountText = if (session.imageCount == 1) {
                            stringResource(Res.string.history_photo_count_one)
                        } else {
                            stringResource(Res.string.history_photo_count, session.imageCount)
                        }
                        Text(
                            text = photoCountText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatRelativeTime(session.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.history_delete),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
