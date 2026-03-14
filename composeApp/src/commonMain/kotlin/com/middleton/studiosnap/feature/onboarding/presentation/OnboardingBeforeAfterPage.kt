package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.presentation.components.SharedBeforeAfterSlider
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.studioSnapTextStyles
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.next_button
import studiosnap.composeapp.generated.resources.onboarding_after_label
import studiosnap.composeapp.generated.resources.onboarding_after_studio
import studiosnap.composeapp.generated.resources.onboarding_before
import studiosnap.composeapp.generated.resources.onboarding_before_after_headline
import studiosnap.composeapp.generated.resources.onboarding_before_after_subheadline
import studiosnap.composeapp.generated.resources.onboarding_before_label

private val ButtonShape = RoundedCornerShape(20.dp)

@Composable
fun OnboardingBeforeAfterPage(
    isPageSettled: Boolean = true,
    onNext: () -> Unit
) {
    val extendedColors = LocalExtendedColorScheme.current

    // Auto-reveal slider animation
    val sliderAnimatable = remember { Animatable(1f) }

    LaunchedEffect(isPageSettled) {
        if (isPageSettled) {
            sliderAnimatable.snapTo(1f)
            sliderAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(2000, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.onboarding_before_after_headline),
                style = studioSnapTextStyles().onboardingHeadline,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.onboarding_before_after_subheadline),
                style = studioSnapTextStyles().onboardingSubheadline,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            SharedBeforeAfterSlider(
                sliderPosition = sliderAnimatable.value,
                beforeLabel = UiText.StringResource(Res.string.onboarding_before_label),
                afterLabel = UiText.StringResource(Res.string.onboarding_after_label),
                beforeColor = AppColors.DarkTextTertiary,
                afterColor = AppColors.PrimaryBlue,
                beforeImage = Res.drawable.onboarding_before,
                afterImage = Res.drawable.onboarding_after_studio,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 42.dp)
                .fillMaxWidth(0.55f)
                .height(44.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            contentPadding = PaddingValues(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = stringResource(Res.string.next_button),
                style = studioSnapTextStyles().buttonText.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = AppColors.PrimaryBlue
            )
        }
    }
}
