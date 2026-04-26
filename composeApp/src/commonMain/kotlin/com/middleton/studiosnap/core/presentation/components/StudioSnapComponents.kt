package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        border = null, // Remove border for filled style
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = 2.dp,
            pressedElevation = 4.dp
        )
    )
}

/**
 * Standard StudioSnap card with elevation in light mode and border in dark mode
 */
@Composable
fun StudioSnapCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = if (isDark) {
            CardDefaults.cardElevation(defaultElevation = 0.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = elevation)
        },
        border = if (isDark) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        } else null
    ) {
        content()
    }
}

/**
 * Section label — small uppercase text for section headers
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

private val GradientButtonShape = RoundedCornerShape(18.dp)

/**
 * Branded gradient button — green diagonal gradient with glow shadow.
 * Used for primary actions (Generate, Continue, Share, etc.).
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val enabledGradient = remember {
        Brush.linearGradient(
            colors = listOf(AppColors.PrimaryGreen, AppColors.PrimaryGreenDark),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
    val disabledGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                AppColors.PaperMid,
                AppColors.PaperDeep
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = GradientButtonShape,
                        ambientColor = AppColors.PrimaryGreen.copy(alpha = 0.32f),
                        spotColor = AppColors.PrimaryGreen.copy(alpha = 0.32f)
                    )
                } else Modifier
            )
            .background(
                brush = if (enabled) enabledGradient else disabledGradient,
                shape = GradientButtonShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
            color = if (enabled) Color.White else AppColors.Ink30
        )
    }
}

/**
 * Secondary button — white/surface background with subtle border.
 * Used for secondary actions (Done, Cancel, etc.).
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 1.dp,
                shape = GradientButtonShape
            )
            .background(
                color = if (isDark) AppColors.DarkSurface else AppColors.White,
                shape = GradientButtonShape
            )
            .then(
                if (!isDark) {
                    Modifier.background(
                        color = Color.Transparent,
                        shape = GradientButtonShape
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            color = if (isDark) Color.White else AppColors.Ink
        )
    }
}
