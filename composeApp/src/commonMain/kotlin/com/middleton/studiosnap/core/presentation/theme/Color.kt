package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object AppColors {
    // Core Palette - Primary Accent
    val PrimaryBlue = Color(0xFF0066FF)
    
    // Status Colors
    val Success = Color(0xFF00B894)
    val Warning = Color(0xFFF59E0B) // Light mode warning
    val WarningDark = Color(0xFFFDCB6E) // Dark mode warning
    val Error = Color(0xFFE74C3C)
    
    // Light Mode - Premium polish colors
    val LightBackground = Color(0xFFFAFAFA) // Warmer background
    val LightSurface = Color(0xFFFFFFFF) // Cards float on the warmer background
    val LightSurfaceElevated = Color(0xFFFFFFFF)
    val LightTextPrimary = Color(0xFF111111) // Softer black
    val LightTextSecondary = Color(0xFF6B7280) // Better contrast
    val LightTextTertiary = Color(0xFF9CA3AF)
    val LightBorder = Color(0xFFE5E7EB)
    val LightDivider = Color(0xFFE5E7EB)
    
    // Dark Mode - Brighter blue for dark backgrounds
    val DarkPrimaryBlue = Color(0xFF3B82F6) // Brighter blue for dark backgrounds
    val DarkBackground = Color(0xFF111111)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkSurfaceElevated = Color(0xFF2E2E2E)
    val DarkTextPrimary = Color(0xFFF9FAFB)
    val DarkTextSecondary = Color(0xFF9CA3AF)
    val DarkTextTertiary = Color(0xFF6B7280)
    val DarkBorder = Color.White.copy(alpha = 0.08f)
    val DarkDivider = Color.White.copy(alpha = 0.08f)
    
    // Special tints and shadows
    val PrimaryTint = PrimaryBlue.copy(alpha = 0.08f) // For card backgrounds
    val CardShadow = Color.Black.copy(alpha = 0.08f) // For shadows
    
    // Export chip colors
    val LightUnselectedChip = Color(0xFFF0F0F0)
    val DarkUnselectedChip = Color(0xFF2E2E2E)
}

data class ExtendedColorScheme(
    val success: Color,
    val warning: Color,
    val border: Color,
    val divider: Color,
    val primaryTint: Color,
    val cardShadow: Color,
    val unselectedChip: Color
)

val extendedLight = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.Warning,
    border = AppColors.LightBorder,
    divider = AppColors.LightDivider,
    primaryTint = AppColors.PrimaryTint,
    cardShadow = AppColors.CardShadow,
    unselectedChip = AppColors.LightUnselectedChip
)

val extendedDark = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.WarningDark,
    border = AppColors.DarkBorder,
    divider = AppColors.DarkDivider,
    primaryTint = AppColors.DarkPrimaryBlue.copy(alpha = 0.08f),
    cardShadow = Color.Transparent, // No shadows in dark mode
    unselectedChip = AppColors.DarkUnselectedChip
)

val lightScheme = lightColorScheme(
    primary = AppColors.PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryTint,
    onPrimaryContainer = AppColors.PrimaryBlue,
    secondary = AppColors.PrimaryBlue,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PrimaryBlue.copy(alpha = 0.1f),
    onSecondaryContainer = AppColors.PrimaryBlue,
    tertiary = AppColors.PrimaryBlue,
    onTertiary = Color.White,
    error = AppColors.Error,
    onError = Color.White,
    background = AppColors.LightBackground,
    onBackground = AppColors.LightTextPrimary,
    surface = AppColors.LightSurface,
    onSurface = AppColors.LightTextPrimary,
    surfaceVariant = AppColors.LightSurface,
    onSurfaceVariant = AppColors.LightTextSecondary,
    outline = AppColors.LightBorder,
    outlineVariant = AppColors.LightDivider
)

val darkScheme = darkColorScheme(
    primary = AppColors.DarkPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.DarkPrimaryBlue.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.DarkPrimaryBlue.copy(alpha = 0.9f),
    secondary = AppColors.DarkPrimaryBlue,
    onSecondary = Color.White,
    secondaryContainer = AppColors.DarkPrimaryBlue.copy(alpha = 0.2f),
    onSecondaryContainer = AppColors.DarkPrimaryBlue.copy(alpha = 0.9f),
    tertiary = AppColors.DarkPrimaryBlue,
    onTertiary = Color.White,
    error = AppColors.Error,
    onError = Color.White,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextPrimary,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextPrimary,
    surfaceVariant = AppColors.DarkSurfaceElevated,
    onSurfaceVariant = AppColors.DarkTextSecondary,
    outline = AppColors.DarkBorder,
    outlineVariant = AppColors.DarkDivider
)

val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

@Composable
fun extendedColorScheme(): ExtendedColorScheme = LocalExtendedColorScheme.current