package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object StudioSnapColors {
    val RestoreBlue = Color(0xFF2563EB)
    val EnhanceGold = Color(0xFFF59E0B)
    val PhotoSilver = Color(0xFF64748B)
    val SuccessGreen = Color(0xFF10B981)
    val ProcessingPurple = Color(0xFF8B5CF6)
    val ErrorRed = Color(0xFFEF4444)

    val White = Color.White
    val SoftGray = Color(0xFFF8FAFC)
    val DarkGray = Color(0xFF1E293B)
    val TransparentWhite60 = Color.White.copy(alpha = 0.6f)
    val TransparentBlack20 = Color.Black.copy(alpha = 0.2f)
}

data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

data class ExtendedColorScheme(
    val restore: ColorFamily,
    val enhance: ColorFamily,
    val photo: ColorFamily,
    val success: ColorFamily,
    val processing: ColorFamily,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color
)

val extendedLight = ExtendedColorScheme(
    restore = ColorFamily(
        color = StudioSnapColors.RestoreBlue,
        onColor = Color.White,
        colorContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.RestoreBlue
    ),
    enhance = ColorFamily(
        color = StudioSnapColors.EnhanceGold,
        onColor = Color.White,
        colorContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.EnhanceGold
    ),
    photo = ColorFamily(
        color = StudioSnapColors.PhotoSilver,
        onColor = Color.White,
        colorContainer = StudioSnapColors.PhotoSilver.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.PhotoSilver
    ),
    success = ColorFamily(
        color = StudioSnapColors.SuccessGreen,
        onColor = Color.White,
        colorContainer = StudioSnapColors.SuccessGreen.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.SuccessGreen
    ),
    processing = ColorFamily(
        color = StudioSnapColors.ProcessingPurple,
        onColor = Color.White,
        colorContainer = StudioSnapColors.ProcessingPurple.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.ProcessingPurple
    ),
    surfaceVariant = StudioSnapColors.SoftGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver
)

val extendedDark = ExtendedColorScheme(
    restore = ColorFamily(
        color = StudioSnapColors.RestoreBlue.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.9f)
    ),
    enhance = ColorFamily(
        color = StudioSnapColors.EnhanceGold.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.9f)
    ),
    photo = ColorFamily(
        color = StudioSnapColors.PhotoSilver.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.PhotoSilver.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.PhotoSilver.copy(alpha = 0.9f)
    ),
    success = ColorFamily(
        color = StudioSnapColors.SuccessGreen.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.SuccessGreen.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.SuccessGreen.copy(alpha = 0.9f)
    ),
    processing = ColorFamily(
        color = StudioSnapColors.ProcessingPurple.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.ProcessingPurple.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.ProcessingPurple.copy(alpha = 0.9f)
    ),
    surfaceVariant = StudioSnapColors.DarkGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver.copy(alpha = 0.8f)
)

val lightScheme = lightColorScheme(
    primary = StudioSnapColors.RestoreBlue,
    onPrimary = Color.White,
    primaryContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.1f),
    onPrimaryContainer = StudioSnapColors.RestoreBlue,
    secondary = StudioSnapColors.EnhanceGold,
    onSecondary = Color.White,
    secondaryContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.1f),
    onSecondaryContainer = StudioSnapColors.EnhanceGold,
    tertiary = StudioSnapColors.ProcessingPurple,
    onTertiary = Color.White,
    error = StudioSnapColors.ErrorRed,
    surface = StudioSnapColors.White,
    onSurface = Color.Black,
    surfaceVariant = StudioSnapColors.SoftGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver
)

val darkScheme = darkColorScheme(
    primary = StudioSnapColors.RestoreBlue.copy(alpha = 0.8f),
    onPrimary = Color.Black,
    primaryContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.2f),
    onPrimaryContainer = StudioSnapColors.RestoreBlue.copy(alpha = 0.9f),
    secondary = StudioSnapColors.EnhanceGold.copy(alpha = 0.8f),
    onSecondary = Color.Black,
    secondaryContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.2f),
    onSecondaryContainer = StudioSnapColors.EnhanceGold.copy(alpha = 0.9f),
    tertiary = StudioSnapColors.ProcessingPurple.copy(alpha = 0.8f),
    onTertiary = Color.Black,
    error = StudioSnapColors.ErrorRed.copy(alpha = 0.8f),
    surface = StudioSnapColors.DarkGray,
    onSurface = Color.White,
    surfaceVariant = StudioSnapColors.DarkGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver.copy(alpha = 0.8f)
)

val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

@Composable
fun extendedColorScheme(): ExtendedColorScheme = LocalExtendedColorScheme.current