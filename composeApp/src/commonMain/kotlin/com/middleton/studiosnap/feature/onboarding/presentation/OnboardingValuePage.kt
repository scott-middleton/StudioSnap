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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.studioSnapTextStyles
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.benefit_ai_subtitle
import studiosnap.composeapp.generated.resources.benefit_ai_title
import studiosnap.composeapp.generated.resources.benefit_batch_subtitle
import studiosnap.composeapp.generated.resources.benefit_batch_title
import studiosnap.composeapp.generated.resources.benefit_export_subtitle
import studiosnap.composeapp.generated.resources.benefit_export_title
import studiosnap.composeapp.generated.resources.benefit_free_trial_subtitle
import studiosnap.composeapp.generated.resources.benefit_free_trial_title
import studiosnap.composeapp.generated.resources.benefit_no_subscriptions_subtitle
import studiosnap.composeapp.generated.resources.benefit_no_subscriptions_title
import studiosnap.composeapp.generated.resources.get_started_button
import studiosnap.composeapp.generated.resources.ic_bolt
import studiosnap.composeapp.generated.resources.ic_box
import studiosnap.composeapp.generated.resources.ic_brain
import studiosnap.composeapp.generated.resources.ic_cart
import studiosnap.composeapp.generated.resources.ic_gift
import studiosnap.composeapp.generated.resources.onboarding_value_headline
import studiosnap.composeapp.generated.resources.onboarding_value_subheadline

private val BenefitRowShape = RoundedCornerShape(12.dp)
private val ButtonShape = RoundedCornerShape(20.dp)

@Composable
fun OnboardingValuePage(
    onGetStarted: () -> Unit
) {
    data class Benefit(
        val icon: DrawableResource,
        val title: String,
        val subtitle: String
    )

    val benefits = listOf(
        Benefit(
            icon = Res.drawable.ic_gift,
            title = stringResource(Res.string.benefit_free_trial_title),
            subtitle = stringResource(Res.string.benefit_free_trial_subtitle)
        ),
        Benefit(
            icon = Res.drawable.ic_bolt,
            title = stringResource(Res.string.benefit_no_subscriptions_title),
            subtitle = stringResource(Res.string.benefit_no_subscriptions_subtitle)
        ),
        Benefit(
            icon = Res.drawable.ic_brain,
            title = stringResource(Res.string.benefit_ai_title),
            subtitle = stringResource(Res.string.benefit_ai_subtitle)
        ),
        Benefit(
            icon = Res.drawable.ic_box,
            title = stringResource(Res.string.benefit_batch_title),
            subtitle = stringResource(Res.string.benefit_batch_subtitle)
        ),
        Benefit(
            icon = Res.drawable.ic_cart,
            title = stringResource(Res.string.benefit_export_title),
            subtitle = stringResource(Res.string.benefit_export_subtitle)
        )
    )

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200L)
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.onboarding_value_headline),
                style = studioSnapTextStyles().onboardingHeadline,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.onboarding_value_subheadline),
                style = studioSnapTextStyles().onboardingSubheadline,
                color = Color.White.copy(alpha = 0.7f),
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
                        icon = benefit.icon,
                        title = benefit.title,
                        subtitle = benefit.subtitle,
                        index = index,
                        isVisible = isVisible
                    )
                }
            }
        }

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
                style = studioSnapTextStyles().buttonText.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = AppColors.PrimaryGreen
            )
        }
    }
}

@Composable
private fun AnimatedBenefitItem(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    index: Int,
    isVisible: Boolean
) {
    var animationTriggered by remember { mutableStateOf(false) }
    var iconAnimationTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(index * 150L)
            animationTriggered = true
            delay(200L)
            iconAnimationTriggered = true
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

    val iconScale by animateFloatAsState(
        targetValue = if (iconAnimationTriggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_$index"
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
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .scale(iconScale),
            tint = AppColors.PrimaryGreen
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = studioSnapTextStyles().benefitText.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White.copy(alpha = opacity)
            )
            Text(
                text = subtitle,
                style = studioSnapTextStyles().benefitText.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White.copy(alpha = opacity * 0.7f)
            )
        }
    }
}
