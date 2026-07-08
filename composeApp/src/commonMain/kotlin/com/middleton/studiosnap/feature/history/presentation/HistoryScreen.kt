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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.core.presentation.components.RestorationImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.util.asString
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = extendedColorScheme().paperMid,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.history_empty_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.history_empty_subtitle),
                fontSize = 13.sp,
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
    val maxVisibleThumbnails = 4

    StudioSnapCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        onClick = onClick,
        cornerRadius = 20.dp
    ) {
        Column {
            // Thumbnail strip
            if (session.thumbnailUris.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp)
                ) {
                    val visibleThumbnails = session.thumbnailUris.take(maxVisibleThumbnails)
                    val overflowCount = session.thumbnailUris.size - maxVisibleThumbnails

                    visibleThumbnails.forEach { uri ->
                        RestorationImage(
                            imagePath = uri,
                            contentDescription = stringResource(Res.string.history_product_photo),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (overflowCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(extendedColorScheme().paperMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$overflowCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Meta row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.displayLabel.asString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.02).sp,
                        color = extendedColorScheme().ink,
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
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " · ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRelativeTime(session.createdAt),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .border(
                            width = 1.dp,
                            color = extendedColorScheme().ink10,
                            shape = RoundedCornerShape(11.dp)
                        ),
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.history_delete),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
