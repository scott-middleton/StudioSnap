package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class StudioSnapTextStyles(
    val splashTitle: TextStyle,
    val onboardingHeadline: TextStyle,
    val onboardingSubheadline: TextStyle,
    val benefitText: TextStyle,
    val buttonText: TextStyle,
    val creditChip: TextStyle,
    val processingStatus: TextStyle,
    val priceText: TextStyle,
    val captionText: TextStyle
)

@Composable
fun studioSnapTextStyles(): StudioSnapTextStyles {
    val interFamily = InterFontFamily()
    return StudioSnapTextStyles(
        splashTitle = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        onboardingHeadline = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        onboardingSubheadline = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        benefitText = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        buttonText = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        creditChip = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        processingStatus = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        priceText = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        captionText = TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        )
    )
}
