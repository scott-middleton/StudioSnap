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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.repeatOnLifecycle
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
import androidx.compose.ui.graphics.Brush
import com.middleton.studiosnap.core.presentation.components.GalleryImage
import com.middleton.studiosnap.core.presentation.components.NativeSignInEffect
import com.middleton.studiosnap.core.presentation.components.RestorationImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.util.asString
import androidx.compose.ui.text.style.TextOverflow
import com.middleton.studiosnap.core.presentation.util.formatRelativeTime
import com.middleton.studiosnap.feature.history.domain.model.HistoryItem
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerLauncher
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
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
import studiosnap.composeapp.generated.resources.home_get_credits
import studiosnap.composeapp.generated.resources.home_photo_limit_hint
import studiosnap.composeapp.generated.resources.home_loading_credits
import studiosnap.composeapp.generated.resources.home_sign_in
import studiosnap.composeapp.generated.resources.home_signing_in
import studiosnap.composeapp.generated.resources.home_generate_button
import studiosnap.composeapp.generated.resources.home_photos_header
import studiosnap.composeapp.generated.resources.home_select_style_first
import studiosnap.composeapp.generated.resources.home_remove_photo
import studiosnap.composeapp.generated.resources.home_style_choose
import studiosnap.composeapp.generated.resources.home_style_choose_hint
import studiosnap.composeapp.generated.resources.home_style_tap_to_change
import studiosnap.composeapp.generated.resources.home_title
import studiosnap.composeapp.generated.resources.style_botanical_garden
import studiosnap.composeapp.generated.resources.style_dark_moody
import studiosnap.composeapp.generated.resources.style_marble_luxe
import studiosnap.composeapp.generated.resources.style_rustic_wood
import studiosnap.composeapp.generated.resources.home_add_label
import studiosnap.composeapp.generated.resources.ic_bolt
import studiosnap.composeapp.generated.resources.ic_transform_photos
import studiosnap.composeapp.generated.resources.ic_diamond
import studiosnap.composeapp.generated.resources.ic_chevron_right
import studiosnap.composeapp.generated.resources.ic_aspect_ratio
import studiosnap.composeapp.generated.resources.ic_palette
import studiosnap.composeapp.generated.resources.credits_count
import studiosnap.composeapp.generated.resources.home_credits_error
import studiosnap.composeapp.generated.resources.home_recent_title
import studiosnap.composeapp.generated.resources.home_recent_view_all

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

    NativeSignInEffect(
        showSignIn = uiState.showSignIn,
        onResult = { success -> viewModel.handleAction(HomeUiAction.OnSignInResult(success)) }
    )

    // Reset transient states (showGalleryPicker, showSignIn) when returning to this screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.handleAction(HomeUiAction.OnScreenResumed)
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
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.3).sp
                        )
                    },
                    actions = {
                        CreditBalancePill(
                            creditLoadingState = state.creditLoadingState,
                            isSigningIn = state.isSigningIn,
                            onClick = { onAction(HomeUiAction.OnCreditBalanceClicked) }
                        )
                        NavIconButton(
                            onClick = { onAction(HomeUiAction.OnHistoryClicked) },
                            contentDescription = stringResource(Res.string.content_history)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
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
                visible = state.photos.isNotEmpty() || !state.isSignedIn,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                GenerateBottomBar(
                    state = state,
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

            if (state.recentGenerations.isNotEmpty()) {
                RecentGenerationsSection(
                    items = state.recentGenerations,
                    onViewAll = { onAction(HomeUiAction.OnViewAllHistoryClicked) },
                    onItemClicked = { onAction(HomeUiAction.OnRecentGenerationClicked(it)) }
                )
            }
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
        modifier = Modifier
            .size(38.dp)
            .border(1.dp, extendedColorScheme().ink10, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
    Spacer(modifier = Modifier.width(7.dp))
}

@Composable
private fun CreditBalancePill(
    creditLoadingState: UserCreditLoadingState,
    isSigningIn: Boolean,
    onClick: () -> Unit
) {
    val gradientBrush = remember(creditLoadingState) {
        when {
            creditLoadingState is UserCreditLoadingState.Loaded &&
                    creditLoadingState.credits.amount == 0 ->
                Brush.linearGradient(listOf(AppColors.PrimaryGreen.copy(alpha = 0.6f), AppColors.PrimaryGreenDark.copy(alpha = 0.6f)))
            else ->
                Brush.linearGradient(listOf(AppColors.PrimaryGreen, AppColors.PrimaryGreenDark))
        }
    }
    val borderBrush = remember {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.1f))
        )
    }

    Box(
        modifier = Modifier
            .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(gradientBrush)
            .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 72.dp)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (creditLoadingState) {
                UserCreditLoadingState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                is UserCreditLoadingState.Loaded -> {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Text(
                        text = pluralStringResource(
                            Res.plurals.credits_count,
                            creditLoadingState.credits.amount,
                            creditLoadingState.credits.amount
                        ),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.1).sp
                    )
                }
                UserCreditLoadingState.LoggedOut -> {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = stringResource(Res.string.home_sign_in),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                UserCreditLoadingState.Error -> {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Text(
                        text = stringResource(Res.string.home_credits_error),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// endregion

// region — Empty State Hero

@Composable
private fun EmptyStateHero(onAddPhotos: () -> Unit, modifier: Modifier = Modifier) {
    val dashedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    val dashedPathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashedBorderColor,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = dashedPathEffect
                    )
                )
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(22.dp))
            .clickable(onClick = onAddPhotos)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Transform icon — two overlapping cards with bolt
        Image(
            painter = painterResource(Res.drawable.ic_transform_photos),
            contentDescription = null,
            modifier = Modifier.size(140.dp, 110.dp),
            contentScale = ContentScale.Fit
        )

        // CTA text
        Text(
            text = stringResource(Res.string.home_add_photos),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryGreen
        )

        // Sub-text
        Text(
            text = stringResource(Res.string.home_photo_limit_hint),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, extendedColorScheme().ink06, RoundedCornerShape(16.dp))
    ) {
        GalleryImage(
            galleryUri = photoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fillContainer = true,
            targetSizePx = 300
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(20.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.home_remove_photo),
                tint = Color.White,
                modifier = Modifier.size(8.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoCell(onClick: () -> Unit) {
    val ext = extendedColorScheme()
    val dashedColor = ext.ink10
    val pathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    }

    Box(
        modifier = Modifier
            .size(PHOTO_CELL_SIZE.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashedColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = pathEffect
                    )
                )
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ext.ink30
        )
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
    val hasSelection = selectedStyle != null

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(text = stringResource(Res.string.home_background_style))

        Card(
            onClick = onChangeStyle,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasSelection) extendedColorScheme().greenTint else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (hasSelection) 3.dp else 2.dp
            ),
            border = if (hasSelection) {
                androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.PrimaryGreen)
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 14.dp, 16.dp, 14.dp),
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
                            selectedStyle.displayName.asString()
                        } else {
                            stringResource(Res.string.home_style_choose)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.1).sp,
                        color = if (hasSelection) AppColors.PrimaryGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (selectedStyle != null) {
                            stringResource(Res.string.home_style_tap_to_change)
                        } else {
                            stringResource(Res.string.home_style_choose_hint)
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chevron arrow box
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            extendedColorScheme().greenTint,
                            RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
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
        Res.drawable.style_botanical_garden,
        Res.drawable.style_marble_luxe,
        Res.drawable.style_rustic_wood,
        Res.drawable.style_dark_moody
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                swatches.take(2).forEach { swatch ->
                    Image(
                        painter = painterResource(swatch),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
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
                            .size(25.dp)
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
    val thumbnailRes = style.thumbnail
    if (thumbnailRes != null) {
        Image(
            painter = painterResource(thumbnailRes),
            contentDescription = style.displayName.asString(),
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback — green box with palette icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(extendedColorScheme().greenTint, RoundedCornerShape(12.dp)),
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
        SectionLabel(
            text = stringResource(Res.string.home_export_format),
            modifier = Modifier.padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
        ) {
            items(ExportFormat.entries) { format ->
                ExportFormatChip(
                    label = exportFormatDisplayName(format),
                    selected = format == exportFormat,
                    onClick = { onExportFormatSelected(format) }
                )
            }
        }
    }
}

@Composable
private fun ExportFormatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) extendedColorScheme().ink else MaterialTheme.colorScheme.surface,
        shadowElevation = if (selected) 2.dp else 1.dp,
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(1.dp, extendedColorScheme().ink10)
        } else null
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.05).sp,
            color = if (selected) MaterialTheme.colorScheme.background else extendedColorScheme().ink70,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
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

// region — Recent Generations Section

@Composable
private fun RecentGenerationsSection(
    items: List<HistoryItem>,
    onViewAll: () -> Unit,
    onItemClicked: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CONTENT_HORIZONTAL_PADDING.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = stringResource(Res.string.home_recent_title))
            Text(
                text = stringResource(Res.string.home_recent_view_all),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryGreen,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = CONTENT_HORIZONTAL_PADDING.dp)
        ) {
            items(items, key = { it.id }) { item ->
                RecentGenerationCard(item = item, onClick = { onItemClicked(item.id) })
            }
        }
    }
}

@Composable
private fun RecentGenerationCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(RECENT_CARD_WIDTH.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        RestorationImage(
            imagePath = item.previewUri,
            contentDescription = null,
            modifier = Modifier
                .width(RECENT_CARD_WIDTH.dp)
                .height(RECENT_CARD_HEIGHT.dp),
            contentScale = ContentScale.Crop
        )
        // Gradient overlay — starts at 55% height
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.65f)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = item.styleName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.1).sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatRelativeTime(item.createdAt),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

// endregion

// region — Bottom Bar

@Composable
private fun GenerateBottomBar(
    state: HomeUiState,
    onGenerate: () -> Unit
) {
    val buttonLabel = when {
        state.isSigningIn -> stringResource(Res.string.home_signing_in)
        !state.isSignedIn -> stringResource(Res.string.home_generate_button)
        state.isLoadingCredits -> stringResource(Res.string.home_loading_credits)
        state.selectedStyle == null -> stringResource(Res.string.home_select_style_first)
        !state.canAffordGeneration && state.hasPhotos ->
            stringResource(Res.string.home_get_credits, state.generationCost)
        else -> stringResource(Res.string.home_generate_preview, state.generationCost)
    }

    // Button is always tappable (signs in, buys credits, or generates) unless loading, signing in, generating, or no style
    val isActionable = !state.isLoadingCredits && !state.isSigningIn && !state.isGenerating && state.selectedStyle != null

    // Fade gradient overlay above button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Background fade — extends upward behind the button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to MaterialTheme.colorScheme.background
                    )
                )
        )

        // Button area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .align(Alignment.BottomCenter)
        ) {
            val gradientBrush = remember {
                Brush.linearGradient(
                    colors = listOf(AppColors.PrimaryGreen, AppColors.PrimaryGreenDark),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            }
            val ext = extendedColorScheme()
            val disabledBrush = Brush.linearGradient(
                colors = listOf(ext.paperMid, ext.paperDeep)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(
                        if (isActionable) {
                            Modifier.shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = AppColors.PrimaryGreen.copy(alpha = 0.32f),
                                spotColor = AppColors.PrimaryGreen.copy(alpha = 0.32f)
                            )
                        } else Modifier
                    )
                    .background(
                        brush = if (isActionable) gradientBrush else disabledBrush,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled = isActionable, onClick = onGenerate),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.isGenerating || state.isSigningIn || state.isLoadingCredits) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_bolt),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isActionable) Color.White else ext.ink30
                        )
                    }
                    Text(
                        text = buttonLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        color = if (isActionable) Color.White else ext.ink30
                    )
                }
            }
        }
    }
}

// endregion

// region — Shared Components

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// endregion

private const val CONTENT_HORIZONTAL_PADDING = 20
private const val CONTENT_TOP_PADDING = 24
private const val CONTENT_BOTTOM_PADDING = 32
private const val SECTION_SPACING = 24
private const val PHOTO_CELL_SIZE = 86
private const val RECENT_CARD_WIDTH = 96
private const val RECENT_CARD_HEIGHT = 128
