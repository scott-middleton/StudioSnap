package com.middleton.studiosnap.feature.results.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultItem
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultsUiState
import com.middleton.studiosnap.feature.results.presentation.viewmodel.ResultsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.results_back_to_home
import studiosnap.composeapp.generated.resources.results_download
import studiosnap.composeapp.generated.resources.results_download_credit
import studiosnap.composeapp.generated.resources.results_downloading
import studiosnap.composeapp.generated.resources.results_empty
import studiosnap.composeapp.generated.resources.results_get_credits
import studiosnap.composeapp.generated.resources.results_photo_counter
import studiosnap.composeapp.generated.resources.results_product_photo
import studiosnap.composeapp.generated.resources.results_purchased
import studiosnap.composeapp.generated.resources.results_share
import studiosnap.composeapp.generated.resources.results_title
import studiosnap.composeapp.generated.resources.results_watermarked

@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<ResultsNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(ResultsUiAction.OnNavigationHandled)
        }
    }

    ResultsScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreenContent(
    state: ResultsUiState,
    onAction: (ResultsUiAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onAction(ResultsUiAction.OnSnackbarDismissed)
        }
    }

    Scaffold(
        topBar = {
            StudioSnapTopBar(
                title = stringResource(Res.string.results_title),
                onBack = { onAction(ResultsUiAction.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        if (state.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.results_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ResultsPager(
                results = state.results,
                creditBalance = state.creditBalance,
                modifier = Modifier.padding(padding),
                onDownload = { onAction(ResultsUiAction.OnDownloadClicked(it)) },
                onShare = { onAction(ResultsUiAction.OnShareClicked(it)) },
                onBuyCredits = { onAction(ResultsUiAction.OnBuyCreditsClicked) },
                onDone = { onAction(ResultsUiAction.OnDoneClicked) }
            )
        }
    }
}

@Composable
private fun ResultsPager(
    results: List<ResultItem>,
    creditBalance: Int,
    modifier: Modifier = Modifier,
    onDownload: (String) -> Unit,
    onShare: (String) -> Unit,
    onBuyCredits: () -> Unit,
    onDone: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { results.size })

    Column(modifier = modifier.fillMaxSize()) {
        if (results.size > 1) {
            Text(
                text = stringResource(
                    Res.string.results_photo_counter,
                    pagerState.currentPage + 1,
                    results.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            ResultPage(
                item = results[page],
                creditBalance = creditBalance,
                onDownload = onDownload,
                onShare = onShare,
                onBuyCredits = onBuyCredits
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = onDone) {
                Text(stringResource(Res.string.results_back_to_home))
            }
        }
    }
}

@Composable
private fun ResultPage(
    item: ResultItem,
    creditBalance: Int,
    onDownload: (String) -> Unit,
    onShare: (String) -> Unit,
    onBuyCredits: () -> Unit
) {
    val result = item.result
    if (result !is GenerationResult.Success) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageUri = item.fullResLocalUri ?: result.previewUri

        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(Res.string.results_product_photo),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    if (result.imageWidth > 0 && result.imageHeight > 0)
                        result.imageWidth.toFloat() / result.imageHeight.toFloat()
                    else 1f
                )
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status badge
        val badgeText = if (item.isPurchased) {
            stringResource(Res.string.results_purchased)
        } else {
            stringResource(Res.string.results_watermarked)
        }

        val badgeColor = if (item.isPurchased) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelMedium,
            color = badgeColor,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = result.styleName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        if (item.isDownloading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.results_downloading))
            }
        } else if (!item.isPurchased) {
            if (creditBalance >= 1) {
                Button(
                    onClick = { onDownload(result.generationId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${stringResource(Res.string.results_download)} — ${stringResource(Res.string.results_download_credit)}")
                }
            } else {
                Button(
                    onClick = onBuyCredits,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(Res.string.results_get_credits))
                }
            }
        }

        if (item.isPurchased) {
            OutlinedButton(
                onClick = { onShare(result.generationId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.results_share))
            }
        }
    }
}
