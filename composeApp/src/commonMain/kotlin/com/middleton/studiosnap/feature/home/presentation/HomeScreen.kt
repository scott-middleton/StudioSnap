package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerLauncher
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_credits
import studiosnap.composeapp.generated.resources.content_history
import studiosnap.composeapp.generated.resources.content_settings
// hero_before/hero_after removed — reserved for onboarding
import studiosnap.composeapp.generated.resources.home_add_photos
import studiosnap.composeapp.generated.resources.home_add_more_photos
import studiosnap.composeapp.generated.resources.home_background_style
import studiosnap.composeapp.generated.resources.home_error_generation_failed
import studiosnap.composeapp.generated.resources.home_error_too_many_photos
import studiosnap.composeapp.generated.resources.home_export_ebay
import studiosnap.composeapp.generated.resources.home_export_etsy
import studiosnap.composeapp.generated.resources.home_export_format
import studiosnap.composeapp.generated.resources.home_export_original
import studiosnap.composeapp.generated.resources.home_export_vinted
import studiosnap.composeapp.generated.resources.home_generate_preview
import studiosnap.composeapp.generated.resources.home_photos_header
import studiosnap.composeapp.generated.resources.home_remove_photo
import studiosnap.composeapp.generated.resources.home_style_choose
import studiosnap.composeapp.generated.resources.home_style_choose_hint
import studiosnap.composeapp.generated.resources.home_style_tap_to_change
import studiosnap.composeapp.generated.resources.home_title
// home_before_label/home_after_label removed — reserved for onboarding
import studiosnap.composeapp.generated.resources.swatch_botanical
import studiosnap.composeapp.generated.resources.swatch_dark
import studiosnap.composeapp.generated.resources.swatch_marble
import studiosnap.composeapp.generated.resources.swatch_wood
import studiosnap.composeapp.generated.resources.home_add_label
import studiosnap.composeapp.generated.resources.ic_bolt
import studiosnap.composeapp.generated.resources.ic_transform_photos
import studiosnap.composeapp.generated.resources.ic_diamond
import studiosnap.composeapp.generated.resources.ic_chevron_right
import studiosnap.composeapp.generated.resources.ic_aspect_ratio
import studiosnap.composeapp.generated.resources.ic_palette

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<HomeNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(HomeUiAction.OnNavigationHandled)
        }
    }

    HomeScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorTooMany = stringResource(Res.string.home_error_too_many_photos, HomeUiState.MAX_PHOTOS)
    val errorGenFailed = stringResource(Res.string.home_error_generation_failed)

    if (state.showGalleryPicker) {
        val remainingSlots = HomeUiState.MAX_PHOTOS - state.photos.size
        ImagePickerLauncher(
            maxSelection = remainingSlots,
            onImagesSelected = { results ->
                onAction(HomeUiAction.OnPhotosSelected(results.map { it.uri }))
            },
            onError = { onAction(HomeUiAction.OnPhotoPickerCancelled) },
            onDismiss = { onAction(HomeUiAction.OnPhotoPickerCancelled) }
        )
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.home_title),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1.2).sp
                        )
                    },
                    actions = {
                        CreditBalancePill(
                            balance = state.creditBalance,
                            onClick = { onAction(HomeUiAction.OnCreditBalanceClicked) }
                        )
                        NavIconButton(
                            onClick = { onAction(HomeUiAction.OnHistoryClicked) },
                            contentDescription = stringResource(Res.string.content_history)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NavIconButton(
                            onClick = { onAction(HomeUiAction.OnSettingsClicked) },
                            contentDescription = stringResource(Res.string.content_settings)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.photos.isNotEmpty(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                GenerateBottomBar(
                    canGenerate = state.canGenerate,
                    onGenerate = { onAction(HomeUiAction.OnGenerateClicked) }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(top = CONTENT_TOP_PADDING.dp, bottom = CONTENT_BOTTOM_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING.dp)
        ) {
            // Photos section — animates between empty hero and photo row
            AnimatedContent(
                targetState = state.photos.isEmpty(),
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "photos_transition"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStateHero(
                        onAddPhotos = { onAction(HomeUiAction.OnAddPhotosClicked) },
                        modifier = Modifier.padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
                    )
                } else {
                    PhotosRow(
                        photos = state.photos,
                        onAddPhotos = { onAction(HomeUiAction.OnAddPhotosClicked) },
                        onRemovePhoto = { onAction(HomeUiAction.OnPhotoRemoved(it)) }
                    )
                }
            }

            BackgroundStyleSection(
                selectedStyle = state.selectedStyle,
                onChangeStyle = { onAction(HomeUiAction.OnStylePickerClicked) },
                modifier = Modifier.padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
            )

            ExportSizeSection(
                exportFormat = state.exportFormat,
                onExportFormatSelected = { onAction(HomeUiAction.OnExportFormatSelected(it)) }
            )
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                when (it) {
                    HomeError.TooManyPhotos -> errorTooMany
                    HomeError.GenerationFailed -> errorGenFailed
                }
            )
            onAction(HomeUiAction.OnErrorDismissed)
        }
    }
}

// region — Top Bar Components

@Composable
private fun NavIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
private fun CreditBalancePill(
    balance: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = AppColors.PrimaryGreenDark,
        shadowElevation = 2.dp,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_diamond),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
            Text(
                text = "$balance",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// endregion

// region — Empty State Hero

@Composable
private fun EmptyStateHero(onAddPhotos: () -> Unit, modifier: Modifier = Modifier) {
    val dashedBorderColor = MaterialTheme.colorScheme.outline
    val dashedPathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashedBorderColor,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = dashedPathEffect
                    )
                )
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .clickable(onClick = onAddPhotos)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transform icon — two overlapping cards with bolt
        Image(
            painter = painterResource(Res.drawable.ic_transform_photos),
            contentDescription = null,
            modifier = Modifier.size(140.dp, 110.dp),
            contentScale = ContentScale.Fit
        )

        // Compact CTA — the whole card is tappable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = AppColors.PrimaryGreen
            )
            Text(
                text = stringResource(Res.string.home_add_photos),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryGreen
            )
        }
    }
}

// BeforeAfterShowcase + ImageLabel removed — before/after reserved for onboarding

// endregion

// region — Photos Row (horizontal scroller)

@Composable
private fun PhotosRow(
    photos: List<ProductPhoto>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header — with horizontal padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(
                text = stringResource(
                    Res.string.home_photos_header,
                    photos.size,
                    HomeUiState.MAX_PHOTOS
                )
            )
            if (photos.size < HomeUiState.MAX_PHOTOS) {
                Surface(
                    onClick = onAddPhotos,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppColors.PrimaryGreen
                        )
                        Text(
                            text = stringResource(Res.string.home_add_more_photos),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryGreen
                        )
                    }
                }
            }
        }

        // Horizontal scrolling row — edge-to-edge with internal padding
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoCell(
                    photoUri = photo.localUri,
                    onRemove = { onRemovePhoto(photo.id) }
                )
            }
            if (photos.size < HomeUiState.MAX_PHOTOS) {
                item {
                    AddPhotoCell(onClick = onAddPhotos)
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(
    photoUri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(PHOTO_CELL_SIZE.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(5.dp)
                )
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.home_remove_photo),
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoCell(onClick: () -> Unit) {
    val dashedColor = MaterialTheme.colorScheme.outline
    val pathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
    }

    Box(
        modifier = Modifier
            .size(PHOTO_CELL_SIZE.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashedColor,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                )
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.home_add_label),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// endregion

// region — Background Style Section

@Composable
private fun BackgroundStyleSection(
    selectedStyle: Style?,
    onChangeStyle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderWithIcon(
            text = stringResource(Res.string.home_background_style),
            iconContent = {
                Icon(
                    painter = painterResource(Res.drawable.ic_palette),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.PrimaryGreen
                )
            }
        )

        Card(
            onClick = onChangeStyle,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Show selected style thumbnail or 2x2 swatch grid
                if (selectedStyle != null) {
                    SelectedStyleThumbnail(style = selectedStyle)
                } else {
                    StyleSwatchGrid()
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedStyle != null) {
                            resolveStyleName(selectedStyle.nameKey)
                        } else {
                            stringResource(Res.string.home_style_choose)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedStyle != null) {
                            stringResource(Res.string.home_style_tap_to_change)
                        } else {
                            stringResource(Res.string.home_style_choose_hint)
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chevron arrow
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.PrimaryGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleSwatchGrid() {
    val swatches = listOf(
        Res.drawable.swatch_botanical,
        Res.drawable.swatch_marble,
        Res.drawable.swatch_wood,
        Res.drawable.swatch_dark
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                swatches.take(2).forEach { swatch ->
                    Image(
                        painter = painterResource(swatch),
                        contentDescription = null,
                        modifier = Modifier
                            .size(27.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                swatches.drop(2).forEach { swatch ->
                    Image(
                        painter = painterResource(swatch),
                        contentDescription = null,
                        modifier = Modifier
                            .size(27.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedStyleThumbnail(style: Style) {
    val thumbnailRes = resolveStyleThumbnail(style.thumbnailResName)
    if (thumbnailRes != null) {
        Image(
            painter = painterResource(thumbnailRes),
            contentDescription = resolveStyleName(style.nameKey),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, AppColors.PrimaryGreen, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback — green box with palette icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(AppColors.PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(2.dp, AppColors.PrimaryGreen, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_palette),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppColors.PrimaryGreen
            )
        }
    }
}

// endregion

// region — Export Size Section

@Composable
private fun ExportSizeSection(
    exportFormat: ExportFormat,
    onExportFormatSelected: (ExportFormat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderWithIcon(
            text = stringResource(Res.string.home_export_format),
            modifier = Modifier.padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp),
            iconContent = {
                Icon(
                    painter = painterResource(Res.drawable.ic_aspect_ratio),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.PrimaryGreen
                )
            }
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
        ) {
            items(ExportFormat.entries) { format ->
                StudioSnapFilterChip(
                    label = exportFormatDisplayName(format),
                    selected = format == exportFormat,
                    onClick = { onExportFormatSelected(format) }
                )
            }
        }
    }
}

@Composable
private fun exportFormatDisplayName(format: ExportFormat): String {
    return stringResource(
        when (format) {
            ExportFormat.ETSY_SQUARE -> Res.string.home_export_etsy
            ExportFormat.EBAY_LANDSCAPE -> Res.string.home_export_ebay
            ExportFormat.VINTED_PORTRAIT -> Res.string.home_export_vinted
            ExportFormat.ORIGINAL -> Res.string.home_export_original
        }
    )
}

// endregion

// region — Bottom Bar

@Composable
private fun GenerateBottomBar(
    canGenerate: Boolean,
    onGenerate: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 16.dp) // Safe area
        ) {
            Button(
                onClick = onGenerate,
                enabled = canGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryGreen,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = if (canGenerate) {
                    ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                } else {
                    ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_bolt),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.home_generate_preview),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
            }
        }
    }
}

// endregion

// region — Shared Components

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun SectionHeaderWithIcon(
    text: String,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
        SectionLabel(text = text)
    }
}

// endregion

private const val CONTENT_HORIZONTAL_PADDING = 20
private const val CONTENT_TOP_PADDING = 16
private const val CONTENT_BOTTOM_PADDING = 32
private const val SECTION_SPACING = 28
private const val PHOTO_CELL_SIZE = 100
