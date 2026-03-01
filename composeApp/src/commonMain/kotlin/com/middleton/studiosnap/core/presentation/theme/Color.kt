package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object StudioSnapColors {
    val StudioTeal = Color(0xFF0D9488)
    val AccentAmber = Color(0xFFF59E0B)
    val PhotoSilver = Color(0xFF64748B)
    val SuccessGreen = Color(0xFF10B981)
    val ProcessingIndigo = Color(0xFF4F46E5)
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
        color = StudioSnapColors.StudioTeal,
        onColor = Color.White,
        colorContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.StudioTeal
    ),
    enhance = ColorFamily(
        color = StudioSnapColors.AccentAmber,
        onColor = Color.White,
        colorContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.AccentAmber
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
        color = StudioSnapColors.ProcessingIndigo,
        onColor = Color.White,
        colorContainer = StudioSnapColors.ProcessingIndigo.copy(alpha = 0.1f),
        onColorContainer = StudioSnapColors.ProcessingIndigo
    ),
    surfaceVariant = StudioSnapColors.SoftGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver
)

val extendedDark = ExtendedColorScheme(
    restore = ColorFamily(
        color = StudioSnapColors.StudioTeal.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.9f)
    ),
    enhance = ColorFamily(
        color = StudioSnapColors.AccentAmber.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.9f)
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
        color = StudioSnapColors.ProcessingIndigo.copy(alpha = 0.8f),
        onColor = Color.Black,
        colorContainer = StudioSnapColors.ProcessingIndigo.copy(alpha = 0.2f),
        onColorContainer = StudioSnapColors.ProcessingIndigo.copy(alpha = 0.9f)
    ),
    surfaceVariant = StudioSnapColors.DarkGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver.copy(alpha = 0.8f)
)

val lightScheme = lightColorScheme(
    primary = StudioSnapColors.StudioTeal,
    onPrimary = Color.White,
    primaryContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.1f),
    onPrimaryContainer = StudioSnapColors.StudioTeal,
    secondary = StudioSnapColors.AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.1f),
    onSecondaryContainer = StudioSnapColors.AccentAmber,
    tertiary = StudioSnapColors.ProcessingIndigo,
    onTertiary = Color.White,
    error = StudioSnapColors.ErrorRed,
    surface = StudioSnapColors.White,
    onSurface = Color.Black,
    surfaceVariant = StudioSnapColors.SoftGray,
    onSurfaceVariant = StudioSnapColors.PhotoSilver
)

val darkScheme = darkColorScheme(
    primary = StudioSnapColors.StudioTeal.copy(alpha = 0.8f),
    onPrimary = Color.Black,
    primaryContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.2f),
    onPrimaryContainer = StudioSnapColors.StudioTeal.copy(alpha = 0.9f),
    secondary = StudioSnapColors.AccentAmber.copy(alpha = 0.8f),
    onSecondary = Color.Black,
    secondaryContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.2f),
    onSecondaryContainer = StudioSnapColors.AccentAmber.copy(alpha = 0.9f),
    tertiary = StudioSnapColors.ProcessingIndigo.copy(alpha = 0.8f),
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