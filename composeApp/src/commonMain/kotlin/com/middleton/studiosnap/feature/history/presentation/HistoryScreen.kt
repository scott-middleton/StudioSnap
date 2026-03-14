package com.middleton.studiosnap.feature.history.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import coil3.compose.AsyncImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryFilter
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryItem
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
import studiosnap.composeapp.generated.resources.history_filter_all
import studiosnap.composeapp.generated.resources.history_filter_previews
import studiosnap.composeapp.generated.resources.history_filter_purchased
import studiosnap.composeapp.generated.resources.history_product_photo
import studiosnap.composeapp.generated.resources.history_purchased_badge
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
    var deleteDialogItemId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            StudioSnapTopBar(
                title = stringResource(Res.string.history_title),
                onBack = { onAction(HistoryUiAction.OnBackClicked) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterRow(
                currentFilter = state.filter,
                onFilterChanged = { onAction(HistoryUiAction.OnFilterChanged(it)) }
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.isEmpty -> {
                    EmptyHistoryContent()
                }
                else -> {
                    HistoryGrid(
                        items = state.items,
                        onItemClicked = { onAction(HistoryUiAction.OnItemClicked(it)) },
                        onDeleteClicked = { deleteDialogItemId = it }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteDialogItemId?.let { itemId ->
        AlertDialog(
            onDismissRequest = { deleteDialogItemId = null },
            title = { Text(stringResource(Res.string.history_delete_title)) },
            text = { Text(stringResource(Res.string.history_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(HistoryUiAction.OnDeleteClicked(itemId))
                    deleteDialogItemId = null
                }) {
                    Text(stringResource(Res.string.history_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogItemId = null }) {
                    Text(stringResource(Res.string.history_delete_cancel))
                }
            }
        )
    }
}

@Composable
private fun FilterRow(
    currentFilter: HistoryFilter,
    onFilterChanged: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFilter.entries.forEach { filter ->
            StudioSnapFilterChip(
                label = when (filter) {
                    HistoryFilter.ALL -> stringResource(Res.string.history_filter_all)
                    HistoryFilter.PURCHASED -> stringResource(Res.string.history_filter_purchased)
                    HistoryFilter.PREVIEWS -> stringResource(Res.string.history_filter_previews)
                },
                selected = filter == currentFilter,
                onClick = { onFilterChanged(filter) }
            )
        }
    }
}

@Composable
private fun EmptyHistoryContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📸", fontSize = MaterialTheme.typography.displayMedium.fontSize)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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

@Composable
private fun HistoryGrid(
    items: List<HistoryItem>,
    onItemClicked: (String) -> Unit,
    onDeleteClicked: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            HistoryGridItem(
                imageUri = item.fullResLocalUri ?: item.previewUri,
                styleName = item.styleName,
                isPurchased = item.isPurchased,
                onClick = { onItemClicked(item.id) },
                onDelete = { onDeleteClicked(item.id) }
            )
        }
    }
}

@Composable
private fun HistoryGridItem(
    imageUri: String,
    styleName: String,
    isPurchased: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(Res.string.history_product_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = styleName,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPurchased) {
                        Text(
                            text = stringResource(Res.string.history_purchased_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
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
