package com.middleton.studiosnap.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.theme.studioSnapTextStyles
import com.middleton.studiosnap.feature.splash.presentation.viewmodel.SplashViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.app_logo
import studiosnap.composeapp.generated.resources.app_name
import studiosnap.composeapp.generated.resources.app_tagline
import studiosnap.composeapp.generated.resources.logo
import studiosnap.composeapp.generated.resources.splash_loading
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

// Pre-allocated shapes — avoid allocation during composition
private val LogoCardShape = RoundedCornerShape(28.dp)
private val LogoGlowShape = CircleShape
private val DotShape = CircleShape

// Pre-computed sparkle layout data (relative to logo center)
private data class SparkleLayout(
    val baseAngle: Float,
    val maxRadius: Float,
    val size: Float
)

private val InnerSparkles: List<SparkleLayout> = List(10) { index ->
    val baseAngle = (index * 37.2f + index * index * 2.1f) % 360f
    val radiusVariation = (index * 17) % 60
    val maxRadius = 40f + radiusVariation + (index % 4) * 25f
    val size = when (index % 4) {
        0 -> 1f; 1 -> 2f; 2 -> 3f; else -> 5f
    }
    SparkleLayout(baseAngle, maxRadius, size)
}

private data class OuterSparkleLayout(
    val baseX: Float, // -0.5..0.5 normalized
    val baseY: Float, // -0.5..0.5 normalized
    val rotationSpeed: Float,
    val size: Float
)

private val OuterSparkles: List<OuterSparkleLayout> = List(40) { i ->
    val index = i + 10
    val randomSeed1 = (index * 97 + 13) % 1000
    val randomSeed2 = (index * 83 + 7) % 1000
    val baseX = (randomSeed1 / 1000f - 0.5f) * 0.6f
    val baseY = (randomSeed2 / 1000f - 0.5f) * 0.6f
    val rotationSpeed = (index % 3 + 1) * 0.2f
    val size = when (index % 4) {
        0 -> 1f; 1 -> 2f; 2 -> 3f; else -> 5f
    }
    OuterSparkleLayout(baseX, baseY, rotationSpeed, size)
}

@Composable
fun SplashScreen() {
    val extendedColors = LocalExtendedColorScheme.current
    val viewModel: SplashViewModel = koinViewModel()
    val navigationHandler: NavigationHandler = koinInject()

    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationHandler.handleNavigation(action.navigationCommand)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_animations")

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, easing = EaseInOutCubic)
        )
    }

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
    )

    val loadingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_dots"
    )

    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )

    // Pre-remember gradient brushes
    val glowBrush = remember(glowIntensity, extendedColors) {
        Brush.radialGradient(
            colors = listOf(
                extendedColors.enhance.color.copy(alpha = glowIntensity),
                Color.Transparent
            )
        )
    }

    val progressValue = animationProgress.value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Combined background + sparkles in a single Canvas (avoid stacking two)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPhotoRestorationBackground(progressValue)
            drawPhotoSparkles(progressValue, sparkleRotation)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Glow — draw-phase only via drawBehind
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .drawBehind {
                            drawCircle(brush = glowBrush)
                        }
                        .blur(20.dp)
                )

                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .semantics { contentDescription = "App Logo" },
                    shape = LogoCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.98f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val colorMatrix = remember(progressValue) {
                            ColorMatrix().apply { setToSaturation(progressValue) }
                        }
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = stringResource(Res.string.app_logo),
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(Res.string.app_name),
                style = studioSnapTextStyles.splashTitle.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.app_tagline),
                style = studioSnapTextStyles.onboardingSubheadline.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.8.sp
                ),
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(64.dp))

            val loadingContentDescription = stringResource(Res.string.splash_loading)
            AnimatedLoadingDots(
                modifier = Modifier.semantics { contentDescription = loadingContentDescription },
                progress = loadingProgress
            )
        }
    }
}

@Composable
private fun AnimatedLoadingDots(
    modifier: Modifier = Modifier,
    progress: Float
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotScale = when {
                progress < 0.33f && index == 0 -> 1.3f
                progress in 0.33f..0.66f && index == 1 -> 1.3f
                progress >= 0.66f && index == 2 -> 1.3f
                else -> 1f
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = DotShape
                    )
            )
        }
    }
}

// --- Draw-scope functions (no allocations per frame) ---

private fun DrawScope.drawPhotoRestorationBackground(animationProgress: Float) {
    val sweepPosition = size.width * (1f - animationProgress)

    // Vintage side
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9E9E9E),
                Color(0xFF616161),
                Color(0xFF424242)
            )
        )
    )

    // Restored side
    if (sweepPosition < size.width) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF3B82F6),
                    Color(0xFF6366F1),
                    Color(0xFF8B5CF6)
                )
            ),
            topLeft = androidx.compose.ui.geometry.Offset(sweepPosition, 0f),
            size = androidx.compose.ui.geometry.Size(size.width - sweepPosition, size.height)
        )
    }

    // Sweep line
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent),
            startX = sweepPosition - 30f,
            endX = sweepPosition + 30f
        )
    )
}

private val DegToRad = (kotlin.math.PI / 180.0).toFloat()

private fun DrawScope.drawPhotoSparkles(
    animationProgress: Float,
    rotationOffset: Float
) {
    val logoX = size.width / 2f
    val logoY = size.height * 0.38f
    val sparkleColor = Color.White

    // Inner sparkles (orbit around logo)
    InnerSparkles.forEach { sparkle ->
        val animatedAngle = (sparkle.baseAngle + rotationOffset) % 360f
        val angleRad = animatedAngle * DegToRad
        val orbitRadius = sparkle.maxRadius * animationProgress

        val x = logoX + cos(angleRad) * orbitRadius
        val y = logoY + sin(angleRad) * orbitRadius

        if (x >= -80 && x <= size.width + 80 && y >= -80 && y <= size.height + 80) {
            val alphaVariation = (sin(angleRad * 0.5f + animationProgress * 1.5f) * 0.3f).coerceAtLeast(0f)
            drawCircle(
                color = sparkleColor.copy(alpha = 0.6f + alphaVariation),
                radius = sparkle.size,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }

    // Outer sparkles (scattered, rotating)
    OuterSparkles.forEach { sparkle ->
        val rotationRad = rotationOffset * sparkle.rotationSpeed * DegToRad
        val cosR = cos(rotationRad)
        val sinR = sin(rotationRad)

        val baseX = sparkle.baseX * size.width
        val baseY = sparkle.baseY * size.height
        val rotatedX = baseX * cosR - baseY * sinR
        val rotatedY = baseX * sinR + baseY * cosR

        val x = logoX + rotatedX * animationProgress
        val y = logoY + rotatedY * animationProgress

        if (x >= -80 && x <= size.width + 80 && y >= -80 && y <= size.height + 80) {
            drawCircle(
                color = sparkleColor.copy(alpha = 0.6f),
                radius = sparkle.size,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
