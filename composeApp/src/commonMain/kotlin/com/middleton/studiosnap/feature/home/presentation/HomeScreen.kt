package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeError
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.category_all
import studiosnap.composeapp.generated.resources.category_clothing
import studiosnap.composeapp.generated.resources.category_cosmetics
import studiosnap.composeapp.generated.resources.category_food
import studiosnap.composeapp.generated.resources.category_homeware
import studiosnap.composeapp.generated.resources.category_jewellery
import studiosnap.composeapp.generated.resources.category_other
import studiosnap.composeapp.generated.resources.content_credits
import studiosnap.composeapp.generated.resources.content_history
import studiosnap.composeapp.generated.resources.content_settings
import studiosnap.composeapp.generated.resources.home_add_photos
import studiosnap.composeapp.generated.resources.home_add_photos_subtitle
import studiosnap.composeapp.generated.resources.home_empty_subtitle
import studiosnap.composeapp.generated.resources.home_empty_title
import studiosnap.composeapp.generated.resources.home_export_ebay
import studiosnap.composeapp.generated.resources.home_export_etsy
import studiosnap.composeapp.generated.resources.home_export_format
import studiosnap.composeapp.generated.resources.home_export_original
import studiosnap.composeapp.generated.resources.home_export_vinted
import studiosnap.composeapp.generated.resources.home_generate_preview
import studiosnap.composeapp.generated.resources.home_options_title
import studiosnap.composeapp.generated.resources.home_photos_count
import studiosnap.composeapp.generated.resources.home_reflection
import studiosnap.composeapp.generated.resources.home_remove_photo
import studiosnap.composeapp.generated.resources.home_shadow
import studiosnap.composeapp.generated.resources.home_styles_title
import studiosnap.composeapp.generated.resources.home_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<HomeNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Credit balance pill
                    CreditBalancePill(
                        balance = uiState.creditBalance,
                        onClick = { viewModel.handleAction(HomeUiAction.OnCreditBalanceClicked) }
                    )
                    IconButton(onClick = { viewModel.handleAction(HomeUiAction.OnHistoryClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(Res.string.content_history)
                        )
                    }
                    IconButton(onClick = { viewModel.handleAction(HomeUiAction.OnSettingsClicked) }) {
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
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.canGenerate,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.handleAction(HomeUiAction.OnGenerateClicked) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(stringResource(Res.string.home_generate_preview))
                }
            }
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
                .verticalScroll(rememberScrollState())
        ) {
            // Photo selection section
            PhotoSection(
                photos = uiState.photos,
                onAddPhotos = { /* TODO: Launch gallery picker in Phase 5b */ },
                onRemovePhoto = { viewModel.handleAction(HomeUiAction.OnPhotoRemoved(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Style picker section
            StylePickerSection(
                styles = uiState.styles,
                selectedStyle = uiState.selectedStyle,
                selectedCategory = uiState.selectedCategory,
                onStyleSelected = { viewModel.handleAction(HomeUiAction.OnStyleSelected(it)) },
                onCategorySelected = { viewModel.handleAction(HomeUiAction.OnCategorySelected(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options section
            OptionsSection(
                shadow = uiState.shadow,
                reflection = uiState.reflection,
                exportFormat = uiState.exportFormat,
                onShadowToggled = { viewModel.handleAction(HomeUiAction.OnShadowToggled(it)) },
                onReflectionToggled = { viewModel.handleAction(HomeUiAction.OnReflectionToggled(it)) },
                onExportFormatSelected = { viewModel.handleAction(HomeUiAction.OnExportFormatSelected(it)) }
            )

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // TODO: Use Res.string for error messages when wired up
            snackbarHostState.showSnackbar(
                when (it) {
                    HomeError.TooManyPhotos -> "Maximum ${HomeUiState.MAX_PHOTOS} photos allowed"
                    HomeError.GenerationFailed -> "Generation failed. Please try again."
                }
            )
            viewModel.handleAction(HomeUiAction.OnErrorDismissed)
        }
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
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = null
    )
}

@Composable
private fun PhotoSection(
    photos: List<ProductPhoto>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    if (photos.isEmpty()) {
        // Empty state — add first photo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onAddPhotos),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.home_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Photos strip
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.home_photos_count, photos.size, HomeUiState.MAX_PHOTOS),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoChip(
                    photo = photo,
                    onRemove = { onRemovePhoto(photo.id) }
                )
            }
        }
    }
}

@Composable
private fun PhotoChip(
    photo: ProductPhoto,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(80.dp)) {
        AsyncImage(
            model = photo.localUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        // Remove button
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

@Composable
private fun StylePickerSection(
    styles: List<Style>,
    selectedStyle: Style?,
    selectedCategory: StyleCategory,
    onStyleSelected: (String) -> Unit,
    onCategorySelected: (StyleCategory) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.home_styles_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(StyleCategory.entries) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(categoryDisplayName(category)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Style grid (non-scrollable since parent scrolls)
        // Using FlowRow instead of LazyVerticalGrid to avoid nested scrolling
        StyleGrid(
            styles = styles,
            selectedStyle = selectedStyle,
            onStyleSelected = onStyleSelected
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyleGrid(
    styles: List<Style>,
    selectedStyle: Style?,
    onStyleSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        styles.forEach { style ->
            StyleCard(
                style = style,
                isSelected = style.id == selectedStyle?.id,
                onClick = { onStyleSelected(style.id) },
                modifier = Modifier.weight(1f)
            )
        }
        // Fill remaining slots in last row
        val remainder = styles.size % 3
        if (remainder != 0) {
            repeat(3 - remainder) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StyleCard(
    style: Style,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .then(
                if (isSelected) Modifier.border(
                    BorderStroke(borderWidth, borderColor),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Load style thumbnail from thumbnailResName when assets are ready
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎨",
                    fontSize = 28.sp
                )
            }
            Text(
                text = style.nameKey,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OptionsSection(
    shadow: Boolean,
    reflection: Boolean,
    exportFormat: ExportFormat,
    onShadowToggled: (Boolean) -> Unit,
    onReflectionToggled: (Boolean) -> Unit,
    onExportFormatSelected: (ExportFormat) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.home_options_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Shadow toggle
        OptionRow(
            label = stringResource(Res.string.home_shadow),
            checked = shadow,
            onCheckedChange = onShadowToggled
        )

        // Reflection toggle
        OptionRow(
            label = stringResource(Res.string.home_reflection),
            checked = reflection,
            onCheckedChange = onReflectionToggled
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Export format picker
        Text(
            text = stringResource(Res.string.home_export_format),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ExportFormat.entries) { format ->
                FilterChip(
                    selected = format == exportFormat,
                    onClick = { onExportFormatSelected(format) },
                    label = { Text(exportFormatDisplayName(format)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun categoryDisplayName(category: StyleCategory): String {
    return stringResource(
        when (category) {
            StyleCategory.ALL -> Res.string.category_all
            StyleCategory.CLOTHING -> Res.string.category_clothing
            StyleCategory.JEWELLERY -> Res.string.category_jewellery
            StyleCategory.HOMEWARE -> Res.string.category_homeware
            StyleCategory.COSMETICS -> Res.string.category_cosmetics
            StyleCategory.FOOD -> Res.string.category_food
            StyleCategory.OTHER -> Res.string.category_other
        }
    )
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
