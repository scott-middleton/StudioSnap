package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerLauncher
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_credits
import studiosnap.composeapp.generated.resources.content_history
import studiosnap.composeapp.generated.resources.content_settings
import studiosnap.composeapp.generated.resources.home_add_photos
import studiosnap.composeapp.generated.resources.home_empty_subtitle
import studiosnap.composeapp.generated.resources.home_empty_title
import studiosnap.composeapp.generated.resources.home_error_generation_failed
import studiosnap.composeapp.generated.resources.home_error_too_many_photos
import studiosnap.composeapp.generated.resources.home_generate_preview
import studiosnap.composeapp.generated.resources.home_photos_count
import studiosnap.composeapp.generated.resources.home_remove_photo
import studiosnap.composeapp.generated.resources.home_background_style
import studiosnap.composeapp.generated.resources.home_style_choose
import studiosnap.composeapp.generated.resources.home_style_choose_hint
import studiosnap.composeapp.generated.resources.home_style_tap_to_change
import studiosnap.composeapp.generated.resources.home_title
import studiosnap.composeapp.generated.resources.home_export_format
import studiosnap.composeapp.generated.resources.home_export_etsy
import studiosnap.composeapp.generated.resources.home_export_ebay
import studiosnap.composeapp.generated.resources.home_export_vinted
import studiosnap.composeapp.generated.resources.home_export_original

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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.home_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp // Larger title as per spec
                    )
                },
                actions = {
                    CreditBalancePill(
                        balance = state.creditBalance,
                        onClick = { onAction(HomeUiAction.OnCreditBalanceClicked) }
                    )
                    IconButton(onClick = { onAction(HomeUiAction.OnHistoryClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(Res.string.content_history)
                        )
                    }
                    IconButton(onClick = { onAction(HomeUiAction.OnSettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.content_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp), // New standard padding
            verticalArrangement = Arrangement.spacedBy(24.dp) // Section gaps
        ) {
            PhotoSection(
                photos = state.photos,
                onAddPhotos = { onAction(HomeUiAction.OnAddPhotosClicked) },
                onRemovePhoto = { onAction(HomeUiAction.OnPhotoRemoved(it)) },
                modifier = Modifier.weight(1f)
            )

            SelectedStyleSection(
                selectedStyle = state.selectedStyle,
                onChangeStyle = { onAction(HomeUiAction.OnStylePickerClicked) }
            )

            ExportFormatSection(
                exportFormat = state.exportFormat,
                onExportFormatSelected = { onAction(HomeUiAction.OnExportFormatSelected(it)) }
            )

            // Generate button at bottom (not FAB)
            GenerateButton(
                canGenerate = state.canGenerate,
                onGenerate = { onAction(HomeUiAction.OnGenerateClicked) }
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

@Composable
private fun GenerateButton(
    canGenerate: Boolean,
    onGenerate: () -> Unit
) {
    Button(
        onClick = onGenerate,
        enabled = canGenerate,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Standard button height
        shape = RoundedCornerShape(16.dp), // Rounded corners as per spec
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add, // Using Add as placeholder for magic wand
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.home_generate_preview),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun CreditBalancePill(
    balance: Int,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "$balance",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Text("💎", fontSize = 14.sp)
        },
        shape = RoundedCornerShape(20.dp), // Rounded pill
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary, // Filled blue
            labelColor = MaterialTheme.colorScheme.onPrimary // White text
        ),
        border = null
    )
}

@Composable
private fun PhotoSection(
    photos: List<ProductPhoto>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smaller hero illustration area  
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = extendedColorScheme().primaryTint,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Title and subtitle
            Text(
                text = "Transform your product photos",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Transform your product photos with AI-powered backgrounds",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Prominent CTA button
            Button(
                onClick = onAddPhotos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.home_add_photos),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.home_photos_count, photos.size, HomeUiState.MAX_PHOTOS),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp // Section header size
                    )
                )
                AssistChip(
                    onClick = onAddPhotos,
                    label = { Text(stringResource(Res.string.home_add_photos)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Better spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(photos, key = { it.id }) { photo ->
                    PhotoChip(
                        photoUri = photo.localUri,
                        onRemove = { onRemovePhoto(photo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoChip(
    photoUri: String,
    onRemove: () -> Unit
) {
    StudioSnapCard(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp) // Inset so delete button isn't clipped
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(Res.string.home_remove_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.home_remove_photo),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectedStyleSection(
    selectedStyle: Style?,
    onChangeStyle: () -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.home_background_style),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp // Section header size
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudioSnapCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (selectedStyle != null) {
                MaterialTheme.colorScheme.surface
            } else {
                extendedColorScheme().primaryTint // Blue tint for unselected
            },
            onClick = onChangeStyle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedStyle != null) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedStyle != null) "🎨" else "➕",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (selectedStyle != null) {
                        Text(
                            text = resolveStyleName(selectedStyle.nameKey),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(Res.string.home_style_tap_to_change),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.home_style_choose),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Browse 18 professional backgrounds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportFormatSection(
    exportFormat: ExportFormat,
    onExportFormatSelected: (ExportFormat) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.home_export_format),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp // Section header size
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
