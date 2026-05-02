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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.feature.splash.presentation.viewmodel.SplashViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.app_logo
import studiosnap.composeapp.generated.resources.app_name
import studiosnap.composeapp.generated.resources.app_tagline
import studiosnap.composeapp.generated.resources.logo
import studiosnap.composeapp.generated.resources.splash_loading
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val LogoCardShape = RoundedCornerShape(30.dp)
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
    val baseX: Float,
    val baseY: Float,
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

private const val DegToRad = (PI / 180.0).toFloat()

@Composable
fun SplashScreen() {
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
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
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

    val dotBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot_bounce"
    )

    val progressValue = animationProgress.value

    val glowAlpha = glowIntensity * progressValue
    val glowBrush = remember(glowAlpha) {
        Brush.radialGradient(
            colors = listOf(
                AppColors.PrimaryGreen.copy(alpha = glowAlpha),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBrandRevealBackground(progressValue)
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
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(logoScale)
                        .drawBehind {
                            drawCircle(brush = glowBrush)
                        }
                        .blur(20.dp)
                )

                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(logoScale)
                        .semantics { contentDescription = "App Logo" },
                    shape = LogoCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
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

            Spacer(modifier = Modifier.height(32.dp))

            val titleColor = lerp(
                Color(0xFF888888),
                extendedColorScheme().ink,
                progressValue
            )
            val taglineColor = lerp(
                Color(0xFF555555),
                MaterialTheme.colorScheme.onSurfaceVariant,
                progressValue
            )

            Text(
                text = stringResource(Res.string.app_name),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2.2).sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(Res.string.app_tagline),
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.3.sp,
                color = taglineColor
            )

            Spacer(modifier = Modifier.height(52.dp))

            val loadingContentDescription = stringResource(Res.string.splash_loading)
            BouncingLoadingDots(
                modifier = Modifier.semantics { contentDescription = loadingContentDescription },
                bounceProgress = dotBounce,
                revealProgress = progressValue
            )
        }
    }
}

@Composable
private fun BouncingLoadingDots(
    modifier: Modifier = Modifier,
    bounceProgress: Float,
    revealProgress: Float
) {
    val density = LocalDensity.current
    val bounceHeightPx = with(density) { 4.dp.toPx() }

    val dotColor = lerp(
        Color(0xFF666666),
        AppColors.PrimaryGreen,
        revealProgress
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->
            val phaseOffset = index * 0.22f * 2f * PI.toFloat() / 1.4f
            val dotPhase = bounceProgress + phaseOffset

            val normalizedPhase = (dotPhase % (2f * PI.toFloat())) / (2f * PI.toFloat())
            val bounceOffset = if (normalizedPhase < 0.4f) {
                -sin(normalizedPhase / 0.4f * PI.toFloat()) * bounceHeightPx
            } else if (normalizedPhase < 0.8f) {
                -sin((1f - (normalizedPhase - 0.4f) / 0.4f) * PI.toFloat()) * bounceHeightPx
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset { IntOffset(0, bounceOffset.toInt()) }
                    .drawBehind {
                        drawCircle(color = dotColor)
                    }
            )
        }
    }
}

/**
 * Swipe-to-reveal background: dull grey sweeps to vibrant green-tinted white.
 * The "before" is muted and lifeless, the "after" is bright with green energy.
 */
private fun DrawScope.drawBrandRevealBackground(animationProgress: Float) {
    val sweepPosition = size.width * (1f - animationProgress)

    // "Before" side — dull grey (lifeless, desaturated)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFD0D0D0),
                Color(0xFFBBBBBB),
                Color(0xFFA8A8A8)
            )
        )
    )

    // "After" side — vibrant green gradient to clean white
    if (sweepPosition < size.width) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    AppColors.GreenLight,
                    AppColors.GreenTint,
                    AppColors.Paper
                ),
                startY = 0f,
                endY = size.height * 0.7f
            ),
            topLeft = androidx.compose.ui.geometry.Offset(sweepPosition, 0f),
            size = androidx.compose.ui.geometry.Size(size.width - sweepPosition, size.height)
        )
    }

    // Sweep line — bright green glow at the boundary
    val sweepLineAlpha = if (animationProgress < 0.95f) 0.7f else (1f - animationProgress) * 14f
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                AppColors.PrimaryGreen.copy(alpha = sweepLineAlpha),
                Color.Transparent
            ),
            startX = sweepPosition - 50f,
            endX = sweepPosition + 50f
        )
    )
}

private fun DrawScope.drawPhotoSparkles(
    animationProgress: Float,
    rotationOffset: Float
) {
    val logoX = size.width / 2f
    val logoY = size.height * 0.38f

    // Sparkle color transitions from grey to green as reveal sweeps
    val sparkleColor = lerp(
        Color(0xFFAAAAAA),
        AppColors.PrimaryGreen,
        animationProgress
    )

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
                color = sparkleColor.copy(alpha = 0.5f + alphaVariation),
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
                color = sparkleColor.copy(alpha = 0.4f),
                radius = sparkle.size,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
