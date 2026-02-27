package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object ImageCloneAiColors {
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
        color = ImageCloneAiColors.RestoreBlue,
        onColor = Color.White,
        colorContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.1f),
        onColorContainer = ImageCloneAiColors.RestoreBlue
    ),
    enhance = ColorFamily(
        color = ImageCloneAiColors.EnhanceGold,
        onColor = Color.White,
        colorContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.1f),
        onColorContainer = ImageCloneAiColors.EnhanceGold
    ),
    photo = ColorFamily(
        color = ImageCloneAiColors.PhotoSilver,
        onColor = Color.White,
        colorContainer = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.1f),
        onColorContainer = ImageCloneAiColors.PhotoSilver
    ),
    success = ColorFamily(
        color = ImageCloneAiColors.SuccessGreen,
        onColor = Color.White,
        colorContainer = ImageCloneAiColors.SuccessGreen.copy(alpha = 0.1f),
        onColorContainer = ImageCloneAiColors.SuccessGreen
    ),
    processing = ColorFamily(
        color = ImageCloneAiColors.ProcessingPurple,
        onColor = Color.White,
        colorContainer = ImageCloneAiColors.ProcessingPurple.copy(alpha = 0.1f),
        onColorContainer = ImageCloneAiColors.ProcessingPurple
    ),
    surfaceVariant = ImageCloneAiColors.SoftGray,
    onSurfaceVariant = ImageCloneAiColors.PhotoSilver
)

val extendedDark = ExtendedColorScheme(
    restore = ColorFamily(
        color = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.2f),
        onColorContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.9f)
    ),
    enhance = ColorFamily(
        color = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.2f),
        onColorContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.9f)
    ),
    photo = ColorFamily(
        color = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.2f),
        onColorContainer = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.9f)
    ),
    success = ColorFamily(
        color = ImageCloneAiColors.SuccessGreen.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = ImageCloneAiColors.SuccessGreen.copy(alpha = 0.2f),
        onColorContainer = ImageCloneAiColors.SuccessGreen.copy(alpha = 0.9f)
    ),
    processing = ColorFamily(
        color = ImageCloneAiColors.ProcessingPurple.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = ImageCloneAiColors.ProcessingPurple.copy(alpha = 0.2f),
        onColorContainer = ImageCloneAiColors.ProcessingPurple.copy(alpha = 0.9f)
    ),
    surfaceVariant = ImageCloneAiColors.DarkGray,
    onSurfaceVariant = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.8f)
)

val lightScheme = lightColorScheme(
    primary = ImageCloneAiColors.RestoreBlue,
    onPrimary = Color.White,
    primaryContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.1f),
    onPrimaryContainer = ImageCloneAiColors.RestoreBlue,
    secondary = ImageCloneAiColors.EnhanceGold,
    onSecondary = Color.White,
    secondaryContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.1f),
    onSecondaryContainer = ImageCloneAiColors.EnhanceGold,
    tertiary = ImageCloneAiColors.ProcessingPurple,
    onTertiary = Color.White,
    error = ImageCloneAiColors.ErrorRed,
    surface = ImageCloneAiColors.White,
    onSurface = Color.Black,
    surfaceVariant = ImageCloneAiColors.SoftGray,
    onSurfaceVariant = ImageCloneAiColors.PhotoSilver
)

val darkScheme = darkColorScheme(
    primary = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.8f),
    onPrimary = Color.Black,
    primaryContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.2f),
    onPrimaryContainer = ImageCloneAiColors.RestoreBlue.copy(alpha = 0.9f),
    secondary = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.8f),
    onSecondary = Color.Black,
    secondaryContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.2f),
    onSecondaryContainer = ImageCloneAiColors.EnhanceGold.copy(alpha = 0.9f),
    tertiary = ImageCloneAiColors.ProcessingPurple.copy(alpha = 0.8f),
    onTertiary = Color.Black,
    error = ImageCloneAiColors.ErrorRed.copy(alpha = 0.8f),
    surface = ImageCloneAiColors.DarkGray,
    onSurface = Color.White,
    surfaceVariant = ImageCloneAiColors.DarkGray,
    onSurfaceVariant = ImageCloneAiColors.PhotoSilver.copy(alpha = 0.8f)
)

val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

@Composable
fun extendedColorScheme(): ExtendedColorScheme = LocalExtendedColorScheme.current