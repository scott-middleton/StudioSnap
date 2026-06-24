package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.components.FullScreenDrawableOverlay
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.core.presentation.util.asString
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.presentation.action.StylePickerUiAction
import com.middleton.studiosnap.feature.home.presentation.viewmodel.StylePickerViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_close
import studiosnap.composeapp.generated.resources.ic_palette
import studiosnap.composeapp.generated.resources.style_picker_empty_category
import studiosnap.composeapp.generated.resources.style_picker_selected
import studiosnap.composeapp.generated.resources.style_picker_title
import studiosnap.composeapp.generated.resources.style_picker_use_this_style
import studiosnap.composeapp.generated.resources.style_picker_view_fullscreen

@Composable
fun StylePickerScreen(
    currentSelectedStyleId: String?,
    viewModel: StylePickerViewModel = koinViewModel(),
    onStyleSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentSelectedStyleId) {
        viewModel.handleAction(StylePickerUiAction.OnInitialise(currentSelectedStyleId))
    }

    val heroStyle = uiState.allStyles.find { it.id == uiState.heroStyleId }

    StylePickerScreenContent(
        styles = uiState.styles,
        heroStyleId = uiState.heroStyleId,
        heroStyle = heroStyle,
        isHeroUnconfirmedPreview = uiState.isHeroUnconfirmedPreview,
        selectedCategory = uiState.selectedCategory,
        onCategorySelected = { viewModel.handleAction(StylePickerUiAction.OnCategorySelected(it)) },
        onStylePreviewed = { viewModel.handleAction(StylePickerUiAction.OnStylePreviewed(it)) },
        onStyleSelected = onStyleSelected,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StylePickerScreenContent(
    styles: List<Style>,
    heroStyleId: String?,
    heroStyle: Style?,
    isHeroUnconfirmedPreview: Boolean,
    selectedCategory: StyleCategory,
    onCategorySelected: (StyleCategory) -> Unit,
    onStylePreviewed: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var fullScreenStyle by remember { mutableStateOf<Style?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.style_picker_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.0).sp
                        )
                    },
                    actions = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.content_close)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            if (styles.isEmpty() && selectedCategory != StyleCategory.ALL) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryChipRow(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        edgePadding = 20.dp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.style_picker_empty_category),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (heroStyle != null) {
                        SelectedStyleHero(
                            style = heroStyle,
                            showSelectedBadge = !isHeroUnconfirmedPreview,
                            onClick = { fullScreenStyle = heroStyle },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onStyleSelected(heroStyle.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(text = stringResource(Res.string.style_picker_use_this_style))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryChipRow(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        edgePadding = 20.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Style cards
                        items(styles, key = { it.id }) { style ->
                            StylePickerCard(
                                styleName = style.displayName.asString(),
                                thumbnail = style.thumbnail,
                                isSelected = style.id == heroStyleId,
                                onClick = { onStylePreviewed(style.id) }
                            )
                        }

                        // Bottom padding
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        fullScreenStyle?.let { style ->
            style.thumbnail?.let { thumbnail ->
                FullScreenDrawableOverlay(
                    thumbnail = thumbnail,
                    contentDescription = style.displayName.asString(),
                    onDismiss = { fullScreenStyle = null }
                )
            }
        }
    }
}

// ─── Selected Style Hero ────────────────────────────────────────────────────

@Composable
private fun SelectedStyleHero(
    style: Style,
    showSelectedBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        if (style.thumbnail != null) {
            Image(
                painter = painterResource(style.thumbnail),
                contentDescription = style.displayName.asString(),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Selected badge
        if (showSelectedBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(
                        color = AppColors.PrimaryGreen,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(Res.string.style_picker_selected),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Style name
        Text(
            text = style.displayName.asString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )

        // Zoom hint
        Icon(
            imageVector = Icons.Default.ZoomIn,
            contentDescription = stringResource(Res.string.style_picker_view_fullscreen),
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .size(22.dp)
        )
    }
}

// ─── Category Chips ─────────────────────────────────────────────────────────

@Composable
private fun CategoryChipRow(
    selectedCategory: StyleCategory,
    onCategorySelected: (StyleCategory) -> Unit,
    edgePadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.width(edgePadding))
        StyleCategory.entries.forEach { category ->
            CategoryChip(
                label = categoryDisplayName(category),
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
        Spacer(modifier = Modifier.width(edgePadding))
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = extendedColorScheme()
    val backgroundColor = if (selected) ext.ink else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) Color.Transparent else ext.ink10

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = backgroundColor,
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        } else null
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

// ─── Style Card ─────────────────────────────────────────────────────────────

@Composable
private fun StylePickerCard(
    styleName: String,
    thumbnail: DrawableResource?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val borderModifier = if (isSelected) {
            Modifier.border(2.5.dp, AppColors.PrimaryGreen, RoundedCornerShape(14.dp))
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp)
                )
                .then(borderModifier)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    painter = painterResource(thumbnail),
                    contentDescription = styleName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_palette),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = styleName,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AppColors.PrimaryGreen else extendedColorScheme().ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
