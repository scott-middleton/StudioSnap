package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object AppColors {
    // Core Palette - Primary Green
    val PrimaryGreen = Color(0xFF10B981)
    val PrimaryGreenDark = Color(0xFF059669)
    val PrimaryGreenTint = Color(0xFFF0FDF4)
    
    // Status Colors
    val Success = Color(0xFF00B894)
    val Warning = Color(0xFFF59E0B) // Light mode warning
    val WarningDark = Color(0xFFFDCB6E) // Dark mode warning
    val Error = Color(0xFFE74C3C)
    
    // Light Mode
    val LightBackground = Color(0xFFF5F5F7)
    val LightSurface = Color(0xFFF5F5F7)
    val LightSurfaceElevated = Color(0xFFFFFFFF)
    val LightTextPrimary = Color(0xFF1A1A1A)
    val LightTextSecondary = Color(0xFF666666)
    val LightTextTertiary = Color(0xFF999999)
    val LightBorder = Color.Black.copy(alpha = 0.08f)
    
    // Splash / Brand Background
    val SplashDarkGreen = Color(0xFF030E0A)
    val SplashMidGreen = Color(0xFF081F16)
    val SplashLightGreen = Color(0xFF0F3328)

    // Dark Mode
    val DarkBackground = Color(0xFF1A1A1A)
    val DarkSurface = Color(0xFF242424)
    val DarkSurfaceElevated = Color(0xFF2E2E2E)
    val DarkTextPrimary = Color(0xFFFFFFFF)
    val DarkTextSecondary = Color(0xFFA0A0A0)
    val DarkTextTertiary = Color(0xFF666666)
    val DarkBorder = Color.White.copy(alpha = 0.1f)
}

data class ExtendedColorScheme(
    val success: Color,
    val warning: Color,
    val border: Color,
    val unselectedChip: Color
)

val extendedLight = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.Warning,
    border = AppColors.LightBorder,
    unselectedChip = AppColors.LightSurface
)

val extendedDark = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.WarningDark,
    border = AppColors.DarkBorder,
    unselectedChip = AppColors.DarkSurfaceElevated
)

val lightScheme = lightColorScheme(
    primary = AppColors.PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.PrimaryGreen,
    secondary = AppColors.PrimaryGreen,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.1f),
    onSecondaryContainer = AppColors.PrimaryGreen,
    tertiary = AppColors.PrimaryGreen,
    onTertiary = Color.White,
    error = AppColors.Error,
    onError = Color.White,
    background = AppColors.LightBackground,
    onBackground = AppColors.LightTextPrimary,
    surface = AppColors.LightSurface,
    onSurface = AppColors.LightTextPrimary,
    surfaceVariant = AppColors.LightSurface,
    onSurfaceVariant = AppColors.LightTextSecondary,
    outline = AppColors.LightBorder
)

val darkScheme = darkColorScheme(
    primary = AppColors.PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.9f),
    secondary = AppColors.PrimaryGreen,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.2f),
    onSecondaryContainer = AppColors.PrimaryGreen.copy(alpha = 0.9f),
    tertiary = AppColors.PrimaryGreen,
    onTertiary = Color.White,
    error = AppColors.Error,
    onError = Color.White,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextPrimary,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextPrimary,
    surfaceVariant = AppColors.DarkSurfaceElevated,
    onSurfaceVariant = AppColors.DarkTextSecondary,
    outline = AppColors.DarkBorder
)

val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

@Composable
fun extendedColorScheme(): ExtendedColorScheme = LocalExtendedColorScheme.current