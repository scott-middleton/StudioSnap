package com.middleton.studiosnap.feature.results.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.FullScreenImageOverlay
import com.middleton.studiosnap.core.presentation.components.GalleryImage
import com.middleton.studiosnap.core.presentation.components.GradientButton
import com.middleton.studiosnap.core.presentation.components.RestorationImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.components.StudioSnapTopBar
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.util.asString
import com.middleton.studiosnap.feature.home.domain.model.GenerationError
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
import studiosnap.composeapp.generated.resources.results_after
import studiosnap.composeapp.generated.resources.results_before
import studiosnap.composeapp.generated.resources.results_done
import studiosnap.composeapp.generated.resources.results_empty
import studiosnap.composeapp.generated.resources.results_error_api
import studiosnap.composeapp.generated.resources.results_error_filtered
import studiosnap.composeapp.generated.resources.results_error_network
import studiosnap.composeapp.generated.resources.results_error_timeout
import studiosnap.composeapp.generated.resources.results_error_unknown
import studiosnap.composeapp.generated.resources.results_photo_counter
import studiosnap.composeapp.generated.resources.results_product_photo
import studiosnap.composeapp.generated.resources.results_saved
import studiosnap.composeapp.generated.resources.results_saving
import studiosnap.composeapp.generated.resources.results_share
import studiosnap.composeapp.generated.resources.results_title

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
    // Pair of (imagePath, aspectRatio) — aspect ratio used for landscape lock in overlay
    var fullScreenImage by remember { mutableStateOf<Pair<String, Float?>?>(null) }

    val snackbarText = state.snackbarMessage?.asString()
    LaunchedEffect(snackbarText) {
        snackbarText?.let {
            snackbarHostState.showSnackbar(it)
            onAction(ResultsUiAction.OnSnackbarDismissed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                EmptyResultsContent(modifier = Modifier.padding(padding))
            } else {
                ResultsContent(
                    state = state,
                    modifier = Modifier.padding(padding),
                    onAction = onAction,
                    onFullScreenClicked = { path, aspectRatio -> fullScreenImage = path to aspectRatio }
                )
            }
        }

        fullScreenImage?.let { (path, aspectRatio) ->
            FullScreenImageOverlay(
                imagePath = path,
                imageAspectRatio = aspectRatio,
                onDismiss = { fullScreenImage = null }
            )
        }
    }
}

// ─── Empty State ────────────────────────────────────────────────────────────

@Composable
private fun EmptyResultsContent(modifier: Modifier = Modifier) {
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.results_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Results Content ────────────────────────────────────────────────────────

@Composable
private fun ResultsContent(
    state: ResultsUiState,
    modifier: Modifier = Modifier,
    onAction: (ResultsUiAction) -> Unit,
    onFullScreenClicked: (path: String, aspectRatio: Float?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.results.size })

    Column(modifier = modifier.fillMaxSize()) {
        // Photo counter
        if (state.results.size > 1) {
            Text(
                text = stringResource(
                    Res.string.results_photo_counter,
                    pagerState.currentPage + 1,
                    state.results.size
                ),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Image pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 16.dp
        ) { page ->
            val item = state.results[page]
            ResultCard(
                item = item,
                isAutoSaving = state.isAutoSaving,
                onToggleBeforeAfter = {
                    val result = item.result
                    if (result is GenerationResult.Success) {
                        onAction(ResultsUiAction.OnToggleBeforeAfter(result.generationId))
                    }
                },
                onFullScreenClicked = {
                    val result = item.result
                    if (result is GenerationResult.Success) {
                        val ar = if (result.imageWidth > 0 && result.imageHeight > 0) {
                            result.imageWidth.toFloat() / result.imageHeight.toFloat()
                        } else null
                        onFullScreenClicked(result.previewUri, ar)
                    }
                }
            )
        }

        // Page indicator
        if (state.results.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            PageIndicator(
                pagerState = pagerState,
                pageCount = state.results.size,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        ActionButtons(
            state = state,
            currentPage = pagerState.currentPage,
            onAction = onAction
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Result Card ────────────────────────────────────────────────────────────

@Composable
private fun ResultCard(
    item: ResultItem,
    isAutoSaving: Boolean,
    onToggleBeforeAfter: () -> Unit,
    onFullScreenClicked: () -> Unit
) {
    val result = item.result

    when (result) {
        is GenerationResult.Success -> SuccessCard(
            result = result,
            showingOriginal = item.showingOriginal,
            isSavedToGallery = item.isSavedToGallery,
            isAutoSaving = isAutoSaving,
            onToggleBeforeAfter = onToggleBeforeAfter,
            onFullScreenClicked = onFullScreenClicked
        )
        is GenerationResult.Failure -> FailureCard(error = result.error)
    }
}

@Composable
private fun SuccessCard(
    result: GenerationResult.Success,
    showingOriginal: Boolean,
    isSavedToGallery: Boolean,
    isAutoSaving: Boolean,
    onToggleBeforeAfter: () -> Unit,
    onFullScreenClicked: () -> Unit
) {
    val aspectRatio = if (result.imageWidth > 0 && result.imageHeight > 0) {
        result.imageWidth.toFloat() / result.imageHeight.toFloat()
    } else {
        1f
    }
    val inputAspectRatio = if (result.inputPhoto.width > 0 && result.inputPhoto.height > 0) {
        result.inputPhoto.width.toFloat() / result.inputPhoto.height.toFloat()
    } else {
        aspectRatio
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Save status — above the image card
        GallerySaveIndicator(isSavedToGallery = isSavedToGallery, isAutoSaving = isAutoSaving)

        Spacer(modifier = Modifier.height(8.dp))

        StudioSnapCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !showingOriginal, onClick = onFullScreenClicked)
            ) {
                AnimatedContent(
                    targetState = showingOriginal,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "before_after"
                ) { isOriginal ->
                    if (isOriginal) {
                        GalleryImage(
                            galleryUri = result.inputPhoto.localUri,
                            contentDescription = stringResource(Res.string.results_product_photo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(inputAspectRatio)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit,
                            knownAspectRatio = inputAspectRatio
                        )
                    } else {
                        RestorationImage(
                            imagePath = result.previewUri,
                            contentDescription = stringResource(Res.string.results_product_photo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Zoom hint — scrim background ensures visibility over any image colour.
                // The entire Box is already tappable — this is a visual affordance only.
                if (!showingOriginal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Before/After toggle pill — below the card so it doesn't overlap the image
        BeforeAfterToggle(
            showingOriginal = showingOriginal,
            onToggle = onToggleBeforeAfter
        )
    }
}

@Composable
private fun FailureCard(error: GenerationError) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        StudioSnapCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (error) {
                    GenerationError.NETWORK -> stringResource(Res.string.results_error_network)
                    GenerationError.TIMEOUT -> stringResource(Res.string.results_error_timeout)
                    GenerationError.API_ERROR -> stringResource(Res.string.results_error_api)
                    GenerationError.CONTENT_FILTERED -> stringResource(Res.string.results_error_filtered)
                    GenerationError.UNKNOWN -> stringResource(Res.string.results_error_unknown)
                },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─── Gallery Save Indicator ──────────────────────────────────────────────────

@Composable
private fun GallerySaveIndicator(isSavedToGallery: Boolean, isAutoSaving: Boolean) {
    val height = Modifier.fillMaxWidth().height(28.dp)
    when {
        isAutoSaving -> Row(
            modifier = height,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.results_saving),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        isSavedToGallery -> Box(
            modifier = height,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.results_saved),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = AppColors.SuccessGreen
            )
        }
        else -> Spacer(modifier = height)
    }
}

// ─── Before/After Toggle ────────────────────────────────────────────────────

@Composable
private fun BeforeAfterToggle(
    showingOriginal: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beforeText = stringResource(Res.string.results_before)
    val afterText = stringResource(Res.string.results_after)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TogglePill(
            text = beforeText,
            selected = showingOriginal
        )
        Spacer(modifier = Modifier.width(2.dp))
        TogglePill(
            text = afterText,
            selected = !showingOriginal
        )
    }
}

@Composable
private fun TogglePill(
    text: String,
    selected: Boolean
) {
    val backgroundColor = if (selected) AppColors.PrimaryGreen else Color.Transparent
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

// ─── Page Indicator ─────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val dotSize by animateDpAsState(
                targetValue = if (isSelected) 8.dp else 6.dp,
                animationSpec = tween(200),
                label = "dot_size"
            )
            val dotColor = if (isSelected) {
                AppColors.PrimaryGreen
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

// ─── Action Buttons ─────────────────────────────────────────────────────────

@Composable
private fun ActionButtons(
    state: ResultsUiState,
    currentPage: Int,
    onAction: (ResultsUiAction) -> Unit
) {
    val currentItem = state.results.getOrNull(currentPage) ?: return
    val result = currentItem.result as? GenerationResult.Success ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Share — primary gradient button
        GradientButton(
            text = stringResource(Res.string.results_share),
            onClick = { onAction(ResultsUiAction.OnShareClicked(result.generationId)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Done — outlined so it has visible weight but is clearly secondary
        OutlinedButton(
            onClick = { onAction(ResultsUiAction.OnDoneClicked) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.results_done),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
