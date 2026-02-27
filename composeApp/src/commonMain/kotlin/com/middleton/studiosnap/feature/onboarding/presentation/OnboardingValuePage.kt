package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.theme.imageCloneAiTextStyles
import com.middleton.studiosnap.core.presentation.theme.ImageCloneAiCorners
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.benefit_no_subscriptions_title
import studiosnap.composeapp.generated.resources.benefit_no_subscriptions_subtitle
import studiosnap.composeapp.generated.resources.benefit_ai_pipeline_title
import studiosnap.composeapp.generated.resources.benefit_ai_pipeline_subtitle
import studiosnap.composeapp.generated.resources.benefit_museum_grade_title
import studiosnap.composeapp.generated.resources.benefit_museum_grade_subtitle
import studiosnap.composeapp.generated.resources.benefit_private_secure_title
import studiosnap.composeapp.generated.resources.benefit_private_secure_subtitle
import studiosnap.composeapp.generated.resources.get_started_button
import studiosnap.composeapp.generated.resources.onboarding_value_headline
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

// Pre-allocated shapes
private val BenefitRowShape = RoundedCornerShape(ImageCloneAiCorners.Medium)
private val ButtonShape = RoundedCornerShape(20.dp)

@Composable
fun OnboardingValuePage(
    animationTrigger: Int = 0,
    onGetStarted: () -> Unit
) {
    val extendedColors = LocalExtendedColorScheme.current

    data class Benefit(val emoji: String, val title: String, val subtitle: String)

    val benefits = listOf(
        Benefit("⚡", stringResource(Res.string.benefit_no_subscriptions_title), stringResource(Res.string.benefit_no_subscriptions_subtitle)),
        Benefit("🧠", stringResource(Res.string.benefit_ai_pipeline_title), stringResource(Res.string.benefit_ai_pipeline_subtitle)),
        Benefit("🎨", stringResource(Res.string.benefit_museum_grade_title), stringResource(Res.string.benefit_museum_grade_subtitle)),
        Benefit("🔒", stringResource(Res.string.benefit_private_secure_title), stringResource(Res.string.benefit_private_secure_subtitle))
    )

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0) {
            isVisible = false
            delay(50L)
            isVisible = true
        }
    }

    // Pre-remember gradient brush
    val gradientBrush = remember(extendedColors) {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.restore.color,
                extendedColors.processing.color
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Static content — Column instead of LazyColumn
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.onboarding_value_headline),
                style = imageCloneAiTextStyles.onboardingHeadline,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                benefits.forEachIndexed { index, benefit ->
                    AnimatedBenefitItem(
                        emoji = benefit.emoji,
                        title = benefit.title,
                        subtitle = benefit.subtitle,
                        index = index,
                        isVisible = isVisible,
                        animationTrigger = animationTrigger
                    )
                }
            }
        }

        // High-contrast button
        Button(
            onClick = onGetStarted,
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
                text = stringResource(Res.string.get_started_button),
                style = imageCloneAiTextStyles.buttonText.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = extendedColors.restore.color
            )
        }
    }
}

@Composable
private fun AnimatedBenefitItem(
    emoji: String,
    title: String,
    subtitle: String,
    index: Int,
    isVisible: Boolean,
    animationTrigger: Int
) {
    var animationTriggered by remember(animationTrigger) { mutableStateOf(false) }
    var emojiAnimationTriggered by remember(animationTrigger) { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(index * 150L)
            animationTriggered = true
            delay(200L)
            emojiAnimationTriggered = true
        }
    }

    val slideOffset by animateFloatAsState(
        targetValue = if (animationTriggered) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "slide_$index"
    )

    val opacity by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "opacity_$index"
    )

    val emojiScale by animateFloatAsState(
        targetValue = if (emojiAnimationTriggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emoji_$index"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = (-slideOffset).dp)
            .alpha(opacity)
            .background(
                Color.White.copy(alpha = 0.08f * opacity),
                BenefitRowShape
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 26.sp,
            modifier = Modifier.scale(emojiScale)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = imageCloneAiTextStyles.benefitText.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White.copy(alpha = opacity)
            )
            Text(
                text = subtitle,
                style = imageCloneAiTextStyles.benefitText.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White.copy(alpha = opacity * 0.7f)
            )
        }
    }
}
