package com.middleton.studiosnap.feature.sessiondetail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.RestorationImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.sessiondetail.presentation.action.SessionDetailUiAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.navigation.SessionDetailNavigationAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.ui_state.SessionDetailUiState
import com.middleton.studiosnap.feature.sessiondetail.presentation.viewmodel.SessionDetailViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.session_detail_delete_cancel
import studiosnap.composeapp.generated.resources.session_detail_delete_confirm
import studiosnap.composeapp.generated.resources.session_detail_delete_message
import studiosnap.composeapp.generated.resources.session_detail_delete_title
import studiosnap.composeapp.generated.resources.session_detail_not_found
import studiosnap.composeapp.generated.resources.session_detail_open_gallery
import studiosnap.composeapp.generated.resources.session_detail_product_photo

@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<SessionDetailNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(SessionDetailUiAction.OnNavigationHandled)
        }
    }

    SessionDetailScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun SessionDetailScreenContent(
    state: SessionDetailUiState,
    onAction: (SessionDetailUiAction) -> Unit
) {
    when (state) {
        SessionDetailUiState.Loading -> SessionDetailLoadingContent()
        SessionDetailUiState.Error -> SessionDetailErrorContent(onBack = { onAction(SessionDetailUiAction.OnBackClicked) })
        is SessionDetailUiState.Success -> SessionDetailSuccessContent(state = state, onAction = onAction)
    }
}

@Composable
private fun SessionDetailLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SessionDetailErrorContent(onBack: () -> Unit) {
    Scaffold(
        topBar = { StudioSnapTopBar(title = "", onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.session_detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionDetailSuccessContent(
    state: SessionDetailUiState.Success,
    onAction: (SessionDetailUiAction) -> Unit
) {
    Scaffold(
        topBar = {
            StudioSnapTopBar(
                title = state.displayLabel,
                onBack = { onAction(SessionDetailUiAction.OnBackClicked) },
                actions = {
                    IconButton(onClick = { onAction(SessionDetailUiAction.OnOpenInGalleryClicked) }) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = stringResource(Res.string.session_detail_open_gallery),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onAction(SessionDetailUiAction.OnDeleteSessionClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.session_detail_delete_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.results, key = { it.generationId }) { result ->
                SessionDetailImageItem(result = result)
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(SessionDetailUiAction.OnDeleteDismissed) },
            title = { Text(stringResource(Res.string.session_detail_delete_title)) },
            text = { Text(stringResource(Res.string.session_detail_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(SessionDetailUiAction.OnDeleteConfirmed) }) {
                    Text(stringResource(Res.string.session_detail_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SessionDetailUiAction.OnDeleteDismissed) }) {
                    Text(stringResource(Res.string.session_detail_delete_cancel))
                }
            }
        )
    }
}

@Composable
private fun SessionDetailImageItem(result: GenerationResult.Success) {
    RestorationImage(
        imagePath = result.previewUri,
        contentDescription = stringResource(Res.string.session_detail_product_photo),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}
