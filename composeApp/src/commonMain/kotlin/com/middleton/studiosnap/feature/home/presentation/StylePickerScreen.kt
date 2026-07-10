package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_close
import studiosnap.composeapp.generated.resources.ic_palette
import studiosnap.composeapp.generated.resources.style_picker_empty_category
import studiosnap.composeapp.generated.resources.style_picker_multi_empty_hint
import studiosnap.composeapp.generated.resources.style_picker_multi_subtitle
import studiosnap.composeapp.generated.resources.style_picker_selected
import studiosnap.composeapp.generated.resources.style_picker_title
import studiosnap.composeapp.generated.resources.style_picker_use_n_styles
import studiosnap.composeapp.generated.resources.style_picker_use_this_style

@Composable
fun StylePickerScreen(
    currentStyleIds: List<String>,
    maxSelectable: Int,
    viewModel: StylePickerViewModel = koinViewModel(),
    onStylesSelected: (List<String>) -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentStyleIds, maxSelectable) {
        viewModel.handleAction(StylePickerUiAction.OnInitialise(currentStyleIds, maxSelectable))
    }

    val heroStyle = uiState.allStyles.find { it.id == uiState.heroStyleId }
    val selectedStyles = uiState.selectedStyleIds.mapNotNull { id -> uiState.allStyles.find { it.id == id } }

    StylePickerScreenContent(
        styles = uiState.styles,
        heroStyleId = uiState.heroStyleId,
        heroStyle = heroStyle,
        isHeroUnconfirmedPreview = uiState.isHeroUnconfirmedPreview,
        selectedCategory = uiState.selectedCategory,
        isMultiSelect = uiState.isMultiSelect,
        selectedStyleIds = uiState.selectedStyleIds,
        selectedStyles = selectedStyles,
        isAtCap = uiState.isAtCap,
        maxSelectable = uiState.maxSelectable,
        capPulse = uiState.capPulse,
        onCategorySelected = { viewModel.handleAction(StylePickerUiAction.OnCategorySelected(it)) },
        onStyleTapped = { styleId ->
            if (uiState.isMultiSelect) {
                viewModel.handleAction(StylePickerUiAction.OnStyleToggled(styleId))
            } else {
                viewModel.handleAction(StylePickerUiAction.OnStylePreviewed(styleId))
            }
        },
        onConfirm = {
            if (uiState.isMultiSelect) {
                if (uiState.selectedStyleIds.isNotEmpty()) onStylesSelected(uiState.selectedStyleIds)
            } else {
                uiState.heroStyleId?.let { onStylesSelected(listOf(it)) }
            }
        },
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
    onStyleTapped: (String) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    isMultiSelect: Boolean = false,
    selectedStyleIds: List<String> = emptyList(),
    selectedStyles: List<Style> = emptyList(),
    isAtCap: Boolean = false,
    maxSelectable: Int = 1,
    capPulse: Int = 0
) {
    var fullScreenStyle by remember { mutableStateOf<Style?>(null) }

    // Brief green flash on the multi-select subtitle when an at-cap tap is rejected.
    var pulsing by remember { mutableStateOf(false) }
    LaunchedEffect(capPulse) {
        if (capPulse == 0) return@LaunchedEffect
        pulsing = true
        delay(150)
        pulsing = false
    }
    val subtitleColor by animateColorAsState(
        targetValue = if (pulsing) AppColors.PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(if (pulsing) 100 else 300),
        label = "capPulseColor"
    )

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
                    if (isMultiSelect) {
                        Text(
                            text = stringResource(Res.string.style_picker_multi_subtitle, maxSelectable),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isMultiSelect || heroStyle != null) {
                        if (isMultiSelect) {
                            SelectedStylesHero(
                                selectedStyles = selectedStyles,
                                onRemove = onStyleTapped,
                                onSegmentTap = { fullScreenStyle = it },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        } else if (heroStyle != null) {
                            SelectedStyleHero(
                                style = heroStyle,
                                showSelectedBadge = !isHeroUnconfirmedPreview,
                                onClick = { fullScreenStyle = heroStyle },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onConfirm,
                            enabled = !isMultiSelect || selectedStyleIds.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = if (isMultiSelect && selectedStyleIds.size > 1) {
                                    stringResource(Res.string.style_picker_use_n_styles, selectedStyleIds.size)
                                } else {
                                    stringResource(Res.string.style_picker_use_this_style)
                                }
                            )
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
                                isSelected = if (isMultiSelect) style.id in selectedStyleIds else style.id == heroStyleId,
                                selectionIndex = if (isMultiSelect) {
                                    selectedStyleIds.indexOf(style.id).takeIf { it >= 0 }?.plus(1)
                                } else null,
                                onClick = { onStyleTapped(style.id) }
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
    }
}

// ─── Selected Styles Hero (multi-select) ────────────────────────────────────

@Composable
private fun SelectedStylesHero(
    selectedStyles: List<Style>,
    onRemove: (String) -> Unit,
    onSegmentTap: (Style) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (selectedStyles.isEmpty()) {
            Text(
                text = stringResource(Res.string.style_picker_multi_empty_hint),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                selectedStyles.forEachIndexed { index, style ->
                    key(style.id) {
                        // New segments compose with animateIn = false, then grow from ~0 to
                        // full weight so the strip visibly divides as selections are added.
                        var animateIn by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { animateIn = true }
                        val animatedWeight by animateFloatAsState(
                            targetValue = if (animateIn) 1f else 0.001f,
                            animationSpec = tween(300),
                            label = "heroSegmentWeight"
                        )
                        HeroSegment(
                            style = style,
                            index = index + 1,
                            onRemove = { onRemove(style.id) },
                            onTap = { onSegmentTap(style) },
                            modifier = Modifier
                                .weight(animatedWeight)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSegment(
    style: Style,
    index: Int,
    onRemove: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clickable(onClick = onTap)) {
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_palette),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom scrim for label legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                        startY = 60f
                    )
                )
        )

        SelectionOrderBadge(
            index = index,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        // Remove button — 14dp icon inside a scrim circle, 32dp touch target
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.content_close),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = style.displayName.asString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
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

// ─── Selection Badge ────────────────────────────────────────────────────────

@Composable
private fun SelectionOrderBadge(
    index: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(AppColors.PrimaryGreen, RoundedCornerShape(100.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$index",
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                // Trim font padding so the digit is optically centered in the circle
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            textAlign = TextAlign.Center
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
    modifier: Modifier = Modifier,
    selectionIndex: Int? = null
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

            // Selection-order badge (multi-select mode only)
            if (selectionIndex != null) {
                SelectionOrderBadge(
                    index = selectionIndex,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
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
