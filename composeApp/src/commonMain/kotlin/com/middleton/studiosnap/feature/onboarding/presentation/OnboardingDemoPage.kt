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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.components.SharedBeforeAfterSlider
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.theme.imageCloneAiTextStyles
import com.middleton.studiosnap.core.presentation.util.asString
import com.middleton.studiosnap.feature.onboarding.domain.model.DemoExample
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.next_button
import org.jetbrains.compose.resources.stringResource

// Pre-allocated shapes
private val CardShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(20.dp)
private val ZoomIconShape = CircleShape
private val CloseFabShape = CircleShape

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OnboardingDemoPage(
    demoExamples: List<DemoExample>,
    isPageSettled: Boolean = true,
    onNext: () -> Unit
) {
    val extendedColors = LocalExtendedColorScheme.current
    var enlargedExampleIndex by remember { mutableIntStateOf(-1) }

    // One-shot slider reveal: animates once when page settles, then stays at 0 (fully revealed)
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

    // Pre-remember gradient brush
    val gradientBrush = remember(extendedColors) {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.restore.color,
                extendedColors.processing.color
            )
        )
    }

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Space for pager dots
                Spacer(modifier = Modifier.height(32.dp))

                // Single title and subtitle for the page
                if (demoExamples.isNotEmpty()) {
                    Text(
                        text = demoExamples.first().title.asString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = demoExamples.first().description.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Before/after images
                demoExamples.forEachIndexed { index, example ->
                    if (enlargedExampleIndex != index) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            DemoExampleItem(
                                example = example,
                                sliderPosition = effectiveSliderPosition,
                                onClick = { enlargedExampleIndex = index },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this,
                                imageKey = DemoImageKey(index, "card")
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 2f)
                                .padding(16.dp)
                        )
                    }

                    if (index < demoExamples.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Button pinned at bottom
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .padding(bottom = 24.dp)
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
                        style = imageCloneAiTextStyles.buttonText.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = extendedColors.restore.color
                    )
                }
            }

            // Enlarged overlay
            AnimatedVisibility(
                visible = enlargedExampleIndex >= 0,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                if (enlargedExampleIndex >= 0) {
                    PlatformBackHandler(enabled = true) {
                        enlargedExampleIndex = -1
                    }

                    val example = demoExamples[enlargedExampleIndex]

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.95f))
                                .clickable { enlargedExampleIndex = -1 },
                            contentAlignment = Alignment.Center
                        ) {
                            SharedBeforeAfterSlider(
                                sliderPosition = effectiveSliderPosition,
                                beforeLabel = example.beforeLabel,
                                afterLabel = example.afterLabel,
                                beforeColor = example.beforeColor,
                                afterColor = example.afterColor,
                                beforeImage = example.beforeImage,
                                afterImage = example.afterImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 2f)
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = DemoImageKey(enlargedExampleIndex, "card")
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

                        // Back press dismisses (PlatformBackHandler above)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DemoExampleItem(
    example: DemoExample,
    sliderPosition: Float,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    imageKey: DemoImageKey
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
                        sharedContentState = rememberSharedContentState(key = imageKey),
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
                        beforeLabel = example.beforeLabel,
                        afterLabel = example.afterLabel,
                        beforeColor = example.beforeColor,
                        afterColor = example.afterColor,
                        beforeImage = example.beforeImage,
                        afterImage = example.afterImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2f)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                ZoomIconShape
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
