package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_back

/**
 * Standard top app bar with back button, used by most secondary screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioSnapTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_back)
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Themed filter chip with primary selection colors — used across Home, History, etc.
 * Now uses filled style with rounded corners for premium look.
 */
@Composable
fun StudioSnapFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = label, 
                fontWeight = FontWeight.Medium
            ) 
        },
        shape = RoundedCornerShape(20.dp), // Pill shape
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = extendedColorScheme().unselectedChip,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = null // Remove border for filled style
    )
}

/**
 * Standard StudioSnap card with elevation in light mode and border in dark mode
 */
@Composable
fun StudioSnapCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Card(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = if (isDark) {
            CardDefaults.cardElevation(defaultElevation = 0.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = 2.dp)
        },
        border = if (isDark) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        } else null
    ) {
        content()
    }
}
