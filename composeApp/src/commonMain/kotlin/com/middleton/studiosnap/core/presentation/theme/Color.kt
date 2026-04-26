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
    val GreenTint = Color(0xFFECFDF5)
    val GreenLight = Color(0xFFD1FAE5)
    val ProcessingTeal = Color(0xFF14B8A6)
    val SuccessGreen = Color(0xFF22C55E)

    // Status Colors
    val Success = Color(0xFF00B894)
    val Warning = Color(0xFFF59E0B)
    val Amber = Color(0xFFF59E0B)
    val WarningDark = Color(0xFFFDCB6E)
    val Error = Color(0xFFE74C3C)

    // New Design System - Paper & Ink
    val Paper = Color(0xFFF8F8F6)
    val PaperMid = Color(0xFFF1F1EE)
    val PaperDeep = Color(0xFFE8E8E4)
    val White = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0F0F0E)
    val Ink70 = Color(0xFF0F0F0E).copy(alpha = 0.70f)
    val Ink50 = Color(0xFF0F0F0E).copy(alpha = 0.50f)
    val Ink30 = Color(0xFF0F0F0E).copy(alpha = 0.30f)
    val Ink10 = Color(0xFF0F0F0E).copy(alpha = 0.10f)
    val Ink06 = Color(0xFF0F0F0E).copy(alpha = 0.06f)
    val Charcoal = Color(0xFF141414)

    // Light Mode
    val LightBackground = Paper
    val LightSurface = Paper
    val LightSurfaceElevated = White
    val LightTextPrimary = Ink
    val LightTextSecondary = Ink70
    val LightTextTertiary = Ink50
    val LightBorder = Ink10

    // Splash / Brand Background (kept for dark variant if needed)
    val SplashDarkGreen = Color(0xFF030E0A)
    val SplashMidGreen = Color(0xFF081F16)
    val SplashLightGreen = Color(0xFF0F3328)

    // Dark Mode
    val DarkBackground = Charcoal
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
    val unselectedChip: Color,
    val paperMid: Color,
    val paperDeep: Color,
    val greenTint: Color,
    val ink: Color,
    val ink70: Color,
    val ink50: Color,
    val ink30: Color,
    val ink10: Color,
    val ink06: Color
)

val extendedLight = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.Warning,
    border = AppColors.LightBorder,
    unselectedChip = AppColors.LightSurface,
    paperMid = AppColors.PaperMid,
    paperDeep = AppColors.PaperDeep,
    greenTint = AppColors.GreenTint,
    ink = AppColors.Ink,
    ink70 = AppColors.Ink70,
    ink50 = AppColors.Ink50,
    ink30 = AppColors.Ink30,
    ink10 = AppColors.Ink10,
    ink06 = AppColors.Ink06
)

val extendedDark = ExtendedColorScheme(
    success = AppColors.Success,
    warning = AppColors.WarningDark,
    border = AppColors.DarkBorder,
    unselectedChip = AppColors.DarkSurfaceElevated,
    paperMid = AppColors.DarkSurface,
    paperDeep = AppColors.DarkSurfaceElevated,
    greenTint = AppColors.PrimaryGreen.copy(alpha = 0.15f),
    ink = AppColors.DarkTextPrimary,
    ink70 = AppColors.DarkTextPrimary.copy(alpha = 0.70f),
    ink50 = AppColors.DarkTextPrimary.copy(alpha = 0.50f),
    ink30 = AppColors.DarkTextPrimary.copy(alpha = 0.30f),
    ink10 = AppColors.DarkTextPrimary.copy(alpha = 0.10f),
    ink06 = AppColors.DarkTextPrimary.copy(alpha = 0.06f)
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