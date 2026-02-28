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
import androidx.compose.foundation.lazy.LazyRow
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
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
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
import studiosnap.composeapp.generated.resources.home_empty_subtitle
import studiosnap.composeapp.generated.resources.home_empty_title
import studiosnap.composeapp.generated.resources.home_error_generation_failed
import studiosnap.composeapp.generated.resources.home_error_too_many_photos
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
import studiosnap.composeapp.generated.resources.style_beach_vibes
import studiosnap.composeapp.generated.resources.style_botanical_garden
import studiosnap.composeapp.generated.resources.style_christmas
import studiosnap.composeapp.generated.resources.style_clean_white
import studiosnap.composeapp.generated.resources.style_concrete_minimal
import studiosnap.composeapp.generated.resources.style_dark_moody
import studiosnap.composeapp.generated.resources.style_gradient_studio
import studiosnap.composeapp.generated.resources.style_marble_luxe
import studiosnap.composeapp.generated.resources.style_morning_kitchen
import studiosnap.composeapp.generated.resources.style_neon_pop
import studiosnap.composeapp.generated.resources.style_paper_craft
import studiosnap.composeapp.generated.resources.style_pastel_dream
import studiosnap.composeapp.generated.resources.style_rustic_wood
import studiosnap.composeapp.generated.resources.style_silk_velvet
import studiosnap.composeapp.generated.resources.style_spring_garden
import studiosnap.composeapp.generated.resources.style_sunset_glow
import studiosnap.composeapp.generated.resources.style_terrazzo
import studiosnap.composeapp.generated.resources.style_warm_linen

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

    // Resolve error messages to strings in the composable
    val errorTooMany = stringResource(Res.string.home_error_too_many_photos, HomeUiState.MAX_PHOTOS)
    val errorGenFailed = stringResource(Res.string.home_error_generation_failed)

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
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.canGenerate,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(HomeUiAction.OnGenerateClicked) },
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
            PhotoSection(
                photos = state.photos,
                onAddPhotos = { /* TODO: Launch gallery picker — wired in platform integration phase */ },
                onRemovePhoto = { onAction(HomeUiAction.OnPhotoRemoved(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            StylePickerSection(
                styles = state.styles,
                selectedStyleId = state.selectedStyle?.id,
                selectedCategory = state.selectedCategory,
                onStyleSelected = { onAction(HomeUiAction.OnStyleSelected(it)) },
                onCategorySelected = { onAction(HomeUiAction.OnCategorySelected(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OptionsSection(
                shadow = state.shadow,
                reflection = state.reflection,
                exportFormat = state.exportFormat,
                onShadowToggled = { onAction(HomeUiAction.OnShadowToggled(it)) },
                onReflectionToggled = { onAction(HomeUiAction.OnReflectionToggled(it)) },
                onExportFormatSelected = { onAction(HomeUiAction.OnExportFormatSelected(it)) }
            )

            // Bottom spacing to clear FAB
            Spacer(modifier = Modifier.height(FAB_CLEARANCE_DP.dp))
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
                    photoUri = photo.localUri,
                    onRemove = { onRemovePhoto(photo.id) }
                )
            }
        }
    }
}

@Composable
private fun PhotoChip(
    photoUri: String,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(80.dp)) {
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

@Composable
private fun StylePickerSection(
    styles: List<Style>,
    selectedStyleId: String?,
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

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(StyleCategory.entries) { category ->
                StudioSnapFilterChip(
                    label = categoryDisplayName(category),
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        StyleGrid(
            styles = styles,
            selectedStyleId = selectedStyleId,
            onStyleSelected = onStyleSelected
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyleGrid(
    styles: List<Style>,
    selectedStyleId: String?,
    onStyleSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        styles.forEach { style ->
            StyleCard(
                styleName = resolveStyleName(style.nameKey),
                isSelected = style.id == selectedStyleId,
                onClick = { onStyleSelected(style.id) },
                modifier = Modifier.weight(1f)
            )
        }
        // Fill remaining slots in last row for even spacing
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
    styleName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isSelected) {
        Modifier.border(
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .then(borderModifier)
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
                text = styleName,
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

        OptionRow(
            label = stringResource(Res.string.home_shadow),
            checked = shadow,
            onCheckedChange = onShadowToggled
        )

        OptionRow(
            label = stringResource(Res.string.home_reflection),
            checked = reflection,
            onCheckedChange = onReflectionToggled
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.home_export_format),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

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

/**
 * Resolves style nameKey (e.g. "style_clean_white") to a localised display name.
 */
@Composable
private fun resolveStyleName(nameKey: String): String {
    return when (nameKey) {
        "style_clean_white" -> stringResource(Res.string.style_clean_white)
        "style_warm_linen" -> stringResource(Res.string.style_warm_linen)
        "style_marble_luxe" -> stringResource(Res.string.style_marble_luxe)
        "style_morning_kitchen" -> stringResource(Res.string.style_morning_kitchen)
        "style_botanical_garden" -> stringResource(Res.string.style_botanical_garden)
        "style_concrete_minimal" -> stringResource(Res.string.style_concrete_minimal)
        "style_sunset_glow" -> stringResource(Res.string.style_sunset_glow)
        "style_beach_vibes" -> stringResource(Res.string.style_beach_vibes)
        "style_dark_moody" -> stringResource(Res.string.style_dark_moody)
        "style_pastel_dream" -> stringResource(Res.string.style_pastel_dream)
        "style_rustic_wood" -> stringResource(Res.string.style_rustic_wood)
        "style_christmas" -> stringResource(Res.string.style_christmas)
        "style_terrazzo" -> stringResource(Res.string.style_terrazzo)
        "style_silk_velvet" -> stringResource(Res.string.style_silk_velvet)
        "style_paper_craft" -> stringResource(Res.string.style_paper_craft)
        "style_spring_garden" -> stringResource(Res.string.style_spring_garden)
        "style_gradient_studio" -> stringResource(Res.string.style_gradient_studio)
        "style_neon_pop" -> stringResource(Res.string.style_neon_pop)
        // Fallback for any future styles not yet in string resources
        else -> nameKey.removePrefix("style_").replace("_", " ")
            .replaceFirstChar { it.uppercase() }
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

private const val FAB_CLEARANCE_DP = 88
