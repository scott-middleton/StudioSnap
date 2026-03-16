package com.middleton.studiosnap.feature.processing.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.components.PickedImage
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.feature.onboarding.presentation.PlatformBackHandler
import com.middleton.studiosnap.feature.processing.presentation.action.ProcessingUiAction
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingStatus
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import com.middleton.studiosnap.feature.processing.presentation.viewmodel.ProcessingViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.processing_cancel
import studiosnap.composeapp.generated.resources.processing_do_not_close
import studiosnap.composeapp.generated.resources.processing_downloading
import studiosnap.composeapp.generated.resources.processing_error_title
import studiosnap.composeapp.generated.resources.processing_generating
import studiosnap.composeapp.generated.resources.processing_go_back
import studiosnap.composeapp.generated.resources.processing_patience
import studiosnap.composeapp.generated.resources.processing_photo_progress
import studiosnap.composeapp.generated.resources.processing_preparing
import studiosnap.composeapp.generated.resources.processing_retry
import studiosnap.composeapp.generated.resources.processing_subtitle_downloading
import studiosnap.composeapp.generated.resources.processing_subtitle_generating
import studiosnap.composeapp.generated.resources.processing_subtitle_preparing
import studiosnap.composeapp.generated.resources.processing_title
import studiosnap.composeapp.generated.resources.processing_your_photo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ButtonShape = RoundedCornerShape(16.dp)
private const val DEG_TO_RAD = (PI / 180.0).toFloat()

@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel = koinViewModel(),
    navigationStrategy: NavigationStrategy<ProcessingNavigationAction> = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationStrategy.navigate(action)
            viewModel.handleAction(ProcessingUiAction.OnNavigationHandled)
        }
    }

    ProcessingScreenContent(
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun ProcessingScreenContent(
    state: ProcessingUiState,
    onAction: (ProcessingUiAction) -> Unit
) {
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                AppColors.PrimaryGreen.copy(alpha = 0.03f),
                AppColors.ProcessingTeal.copy(alpha = 0.06f),
                AppColors.PrimaryGreen.copy(alpha = 0.03f)
            )
        )
    }

    // Prevent back navigation during processing — API call costs money
    PlatformBackHandler(enabled = state is ProcessingUiState.Processing) { /* no-op */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            ProcessingUiState.Loading -> LoadingContent()
            is ProcessingUiState.Processing -> ProcessingContent(
                state = state,
                onCancel = { onAction(ProcessingUiAction.OnCancelClicked) }
            )
            is ProcessingUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { onAction(ProcessingUiAction.OnRetryClicked) },
                onGoBack = { onAction(ProcessingUiAction.OnCancelClicked) }
            )
            ProcessingUiState.Complete -> LoadingContent()
        }
    }
}

// ─── Loading ────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = AppColors.PrimaryGreen
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.processing_title),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// ─── Processing Content ─────────────────────────────────────────────────────

@Composable
private fun ProcessingContent(
    state: ProcessingUiState.Processing,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing_anim")

    var showHint by remember { mutableStateOf(false) }
    LaunchedEffect(state.status) {
        showHint = false
        if (state.status == ProcessingStatus.Generating) {
            delay(5000)
            showHint = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val imageSize = (maxWidth - 70.dp - 56.dp).coerceAtLeast(180.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image preview with animated ring
            ImagePreviewWithRing(
                imageUri = state.currentPhotoUri,
                status = state.status,
                infiniteTransition = infiniteTransition,
                imageSize = imageSize
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Step indicators
            ProcessingSteps(currentStatus = state.status)

            Spacer(modifier = Modifier.height(36.dp))

            // Status text — animated transitions
            AnimatedContent(
                targetState = state.status,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                            fadeOut(tween(200))
                },
                label = "status_text"
            ) { currentStatus ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (currentStatus) {
                            ProcessingStatus.Preparing -> stringResource(Res.string.processing_preparing)
                            ProcessingStatus.Generating -> stringResource(Res.string.processing_generating)
                            ProcessingStatus.Downloading -> stringResource(Res.string.processing_downloading)
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (currentStatus) {
                            ProcessingStatus.Preparing -> stringResource(Res.string.processing_subtitle_preparing)
                            ProcessingStatus.Generating -> stringResource(Res.string.processing_subtitle_generating)
                            ProcessingStatus.Downloading -> stringResource(Res.string.processing_subtitle_downloading)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Photo counter (only for batches)
            if (state.totalPhotos > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        Res.string.processing_photo_progress,
                        state.currentPhotoIndex + 1,
                        state.totalPhotos
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.PrimaryGreen
                )
            }

            // Progress percentage
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = state.overallProgress,
                    animationSpec = tween(300),
                    label = "progress"
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.PrimaryGreen
                )
            }

            // Patience hint
            val hintAlpha by animateFloatAsState(
                targetValue = if (showHint) 1f else 0f,
                animationSpec = tween(600),
                label = "hint_alpha"
            )
            Column(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(Res.string.processing_patience),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * hintAlpha),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.processing_do_not_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f * hintAlpha),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Image Preview with Animated Ring ───────────────────────────────────────

@Composable
private fun ImagePreviewWithRing(
    imageUri: String?,
    status: ProcessingStatus,
    infiniteTransition: InfiniteTransition,
    imageSize: Dp = 180.dp
) {
    // Primary rotation — drives main ring + primary particles
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Counter-rotation for secondary ring — opposite direction, different speed
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Subtle image pulse — reuse glow cycle
    val imagePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "image_pulse"
    )

    // Stage-based ring colours
    val ringColor1 by animateColorAsState(
        targetValue = when (status) {
            ProcessingStatus.Preparing -> AppColors.PrimaryGreen
            ProcessingStatus.Generating -> AppColors.ProcessingTeal
            ProcessingStatus.Downloading -> AppColors.SuccessGreen
        },
        animationSpec = tween(800),
        label = "ring_color1"
    )

    val ringColor2 by animateColorAsState(
        targetValue = when (status) {
            ProcessingStatus.Preparing -> AppColors.ProcessingTeal
            ProcessingStatus.Generating -> AppColors.PrimaryGreen
            ProcessingStatus.Downloading -> AppColors.SuccessGreen.copy(alpha = 0.6f)
        },
        animationSpec = tween(800),
        label = "ring_color2"
    )

    val sweepAngle by animateFloatAsState(
        targetValue = when (status) {
            ProcessingStatus.Preparing -> 200f
            ProcessingStatus.Generating -> 300f
            ProcessingStatus.Downloading -> 340f
        },
        animationSpec = tween(600),
        label = "sweep"
    )

    // Pre-compute particle offsets
    val primaryParticles = remember { listOf(0f, 120f, 240f) }
    val secondaryParticles = remember { listOf(60f, 180f, 300f) }

    // Hoist brushes — glowBrush recomputes every frame (glowAlpha animated), so no remember
    val glowBrush = Brush.radialGradient(
        colors = listOf(
            ringColor1.copy(alpha = glowAlpha),
            ringColor2.copy(alpha = glowAlpha * 0.3f),
            Color.Transparent
        )
    )
    val ringBrush = remember(ringColor1, ringColor2) {
        Brush.sweepGradient(
            colors = listOf(
                ringColor1,
                ringColor2,
                ringColor1.copy(alpha = 0.4f),
                ringColor2.copy(alpha = 0.15f),
                Color.Transparent
            )
        )
    }
    val secondaryRingBrush = remember(ringColor1, ringColor2) {
        Brush.sweepGradient(
            colors = listOf(
                ringColor2.copy(alpha = 0.3f),
                ringColor1.copy(alpha = 0.15f),
                Color.Transparent,
                ringColor2.copy(alpha = 0.2f)
            )
        )
    }

    // Hoist dp→px conversions
    val density = LocalDensity.current
    val ringStrokePx = remember(density) { with(density) { 3.5f.dp.toPx() } }
    val secondaryRingStrokePx = remember(density) { with(density) { 1.5f.dp.toPx() } }
    val ringDiameterPx = remember(imageSize, density) { with(density) { (imageSize + 20.dp).toPx() } }
    val secondaryRingDiameterPx = remember(imageSize, density) { with(density) { (imageSize + 40.dp).toPx() } }
    val particleOrbitRadiusPx = remember(imageSize, density) { with(density) { (imageSize + 28.dp).toPx() / 2f } }
    val outerParticleOrbitPx = remember(imageSize, density) { with(density) { (imageSize + 50.dp).toPx() / 2f } }
    val particleRadiusPx = remember(density) { with(density) { 3f.dp.toPx() } }
    val smallParticleRadiusPx = remember(density) { with(density) { 1.8f.dp.toPx() } }

    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(imageSize + 70.dp)
                .graphicsLayer {}
        ) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            // ── Glow ──────────────────────────────────────────────────────
            drawCircle(brush = glowBrush, radius = size.minDimension / 2f, center = canvasCenter)

            // ── Secondary ring (outer, counter-rotating, thinner) ─────────
            val secondaryRadius = (secondaryRingDiameterPx - secondaryRingStrokePx) / 2f
            val secondaryTopLeft = Offset(canvasCenter.x - secondaryRadius, canvasCenter.y - secondaryRadius)
            val secondarySize = Size(secondaryRadius * 2f, secondaryRadius * 2f)

            drawArc(
                brush = secondaryRingBrush,
                startAngle = counterRotation,
                sweepAngle = 160f,
                useCenter = false,
                style = Stroke(width = secondaryRingStrokePx, cap = StrokeCap.Round),
                topLeft = secondaryTopLeft,
                size = secondarySize
            )

            // ── Main ring ─────────────────────────────────────────────────
            val ringRadius = (ringDiameterPx - ringStrokePx) / 2f
            val ringTopLeft = Offset(canvasCenter.x - ringRadius, canvasCenter.y - ringRadius)
            val ringSize = Size(ringRadius * 2f, ringRadius * 2f)

            drawArc(
                brush = ringBrush,
                startAngle = ringRotation,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = ringStrokePx, cap = StrokeCap.Round),
                topLeft = ringTopLeft,
                size = ringSize
            )

            // ── Primary orbiting particles ─────────────────────────────────
            primaryParticles.forEach { offsetDeg ->
                val angle = (ringRotation + offsetDeg) * DEG_TO_RAD
                val px = canvasCenter.x + cos(angle) * particleOrbitRadiusPx
                val py = canvasCenter.y + sin(angle) * particleOrbitRadiusPx
                drawCircle(
                    color = ringColor1.copy(alpha = 0.55f),
                    radius = particleRadiusPx,
                    center = Offset(px, py)
                )
            }

            // ── Secondary particles (outer orbit, smaller, dimmer) ─────────
            secondaryParticles.forEach { offsetDeg ->
                val angle = (counterRotation + offsetDeg) * DEG_TO_RAD
                val px = canvasCenter.x + cos(angle) * outerParticleOrbitPx
                val py = canvasCenter.y + sin(angle) * outerParticleOrbitPx
                drawCircle(
                    color = ringColor2.copy(alpha = 0.3f),
                    radius = smallParticleRadiusPx,
                    center = Offset(px, py)
                )
            }
        }

        // Image with subtle pulse
        if (imageUri != null) {
            PickedImage(
                imageUri = imageUri,
                contentDescription = stringResource(Res.string.processing_your_photo),
                modifier = Modifier
                    .size(imageSize)
                    .scale(imagePulse)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                },
                error = {
                    ImagePlaceholder()
                }
            )
        } else {
            Box(modifier = Modifier.size(imageSize)) {
                ImagePlaceholder()
            }
        }
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ImageSearch,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Processing Steps ───────────────────────────────────────────────────────

@Composable
private fun ProcessingSteps(currentStatus: ProcessingStatus) {
    val steps = ProcessingStatus.entries

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = step == currentStatus
            val isComplete = step.ordinal < currentStatus.ordinal

            StepDot(
                icon = when (step) {
                    ProcessingStatus.Preparing -> Icons.Default.ImageSearch
                    ProcessingStatus.Generating -> Icons.Default.AutoFixHigh
                    ProcessingStatus.Downloading -> Icons.Default.Download
                },
                isActive = isActive,
                isComplete = isComplete
            )

            if (index < steps.lastIndex) {
                StepConnector(isComplete = isComplete)
            }
        }
    }
}

@Composable
private fun StepDot(
    icon: ImageVector,
    isActive: Boolean,
    isComplete: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = tween(300),
        label = "step_scale"
    )

    val backgroundColor = when {
        isActive -> AppColors.PrimaryGreen
        isComplete -> AppColors.SuccessGreen
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = when {
        isActive || isComplete -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .background(color = backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
    }
}

@Composable
private fun StepConnector(isComplete: Boolean) {
    val connectorColor = if (isComplete) {
        AppColors.PrimaryGreen.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    }

    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(connectorColor, RoundedCornerShape(1.dp))
    )
}

// ─── Error Content ──────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onGoBack: () -> Unit
) {
    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(AppColors.PrimaryGreen, AppColors.ProcessingTeal)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.processing_error_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush, ButtonShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.processing_retry),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGoBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = ButtonShape
        ) {
            Text(
                text = stringResource(Res.string.processing_go_back),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
