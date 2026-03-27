package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.presentation.components.SharedBeforeAfterSlider
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

private val CardShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OnboardingBeforeAfterPage(
    isPageSettled: Boolean = true,
    onNext: () -> Unit
) {
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

    val effectiveSliderPosition = sliderAnimatable.value
    var isEnlarged by remember { mutableStateOf(false) }

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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

                if (!isEnlarged) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        BeforeAfterCard(
                            sliderPosition = effectiveSliderPosition,
                            onClick = { isEnlarged = true },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this
                        )
                    }
                } else {
                    // Placeholder to maintain layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Next button
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
                    color = AppColors.PrimaryGreen
                )
            }

            // Enlarged overlay
            AnimatedVisibility(
                visible = isEnlarged,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                if (isEnlarged) {
                    PlatformBackHandler(enabled = true) {
                        isEnlarged = false
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.95f))
                                .clickable { isEnlarged = false },
                            contentAlignment = Alignment.Center
                        ) {
                            SharedBeforeAfterSlider(
                                sliderPosition = effectiveSliderPosition,
                                beforeLabel = UiText.StringResource(Res.string.onboarding_before_label),
                                afterLabel = UiText.StringResource(Res.string.onboarding_after_label),
                                beforeColor = AppColors.DarkTextTertiary,
                                afterColor = AppColors.PrimaryGreen,
                                beforeImage = Res.drawable.onboarding_before,
                                afterImage = Res.drawable.onboarding_after_studio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 4f)
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = DemoImageKey(0, "card")
                                        ),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        boundsTransform = { _, _ ->
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        }
                                    )
                                    .clickable { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BeforeAfterCard(
    sliderPosition: Float,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "press_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false }
                ) { _, _ -> }
            }
    ) {
        with(sharedTransitionScope) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = DemoImageKey(0, "card")
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        }
                    ),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                )
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    SharedBeforeAfterSlider(
                        sliderPosition = sliderPosition,
                        beforeLabel = UiText.StringResource(Res.string.onboarding_before_label),
                        afterLabel = UiText.StringResource(Res.string.onboarding_after_label),
                        beforeColor = AppColors.DarkTextTertiary,
                        afterColor = AppColors.PrimaryGreen,
                        beforeImage = Res.drawable.onboarding_before,
                        afterImage = Res.drawable.onboarding_after_studio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Tap to enlarge",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
