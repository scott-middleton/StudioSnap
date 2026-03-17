package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.components.StudioSnapCard
import com.middleton.studiosnap.core.presentation.components.StudioSnapFilterChip
import com.middleton.studiosnap.core.presentation.theme.AppColors
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
import studiosnap.composeapp.generated.resources.style_picker_title

@Composable
fun StylePickerScreen(
    currentSelectedStyleId: String?,
    viewModel: StylePickerViewModel = koinViewModel(),
    onStyleSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    StylePickerScreenContent(
        styles = uiState.styles,
        selectedStyleId = currentSelectedStyleId,
        selectedCategory = uiState.selectedCategory,
        onCategorySelected = { viewModel.handleAction(StylePickerUiAction.OnCategorySelected(it)) },
        onStyleSelected = onStyleSelected,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StylePickerScreenContent(
    styles: List<Style>,
    selectedStyleId: String?,
    selectedCategory: StyleCategory,
    onCategorySelected: (StyleCategory) -> Unit,
    onStyleSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.style_picker_title),
                        fontWeight = FontWeight.Bold
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
            // Empty state for filtered category
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChipRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category filter chips as full-width header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        CategoryChipRow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = onCategorySelected
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Style cards
                items(styles, key = { it.id }) { style ->
                    StylePickerCard(
                        styleName = style.displayName.asString(),
                        thumbnail = style.thumbnail,
                        isSelected = style.id == selectedStyleId,
                        onClick = { onStyleSelected(style.id) }
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

// ─── Category Chips ─────────────────────────────────────────────────────────

@Composable
private fun CategoryChipRow(
    selectedCategory: StyleCategory,
    onCategorySelected: (StyleCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyleCategory.entries.forEach { category ->
            StudioSnapFilterChip(
                label = categoryDisplayName(category),
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
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
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, AppColors.PrimaryGreen, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    StudioSnapCard(
        modifier = modifier
            .aspectRatio(0.85f)
            .then(borderModifier),
        backgroundColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
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
            Text(
                text = styleName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
