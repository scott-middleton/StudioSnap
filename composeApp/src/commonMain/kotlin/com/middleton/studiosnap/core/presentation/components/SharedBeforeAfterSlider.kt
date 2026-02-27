package com.middleton.studiosnap.core.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.toUri
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.presentation.util.asString
import com.middleton.studiosnap.core.util.getCurrentTimeMillis
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.error_image_load_failed
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

/**
 * A before/after image comparison slider that supports both DrawableResource images
 * and String image paths (loaded via Coil).
 *
 * @param sliderPosition The animated slider position (0.0 = all before, 1.0 = all after)
 * @param beforeLabel Label displayed for the "before" side
 * @param afterLabel Label displayed for the "after" side
 * @param beforeColor Fallback color for the "before" side when no image is provided
 * @param afterColor Fallback color for the "after" side when no image is provided
 * @param beforeImage Optional DrawableResource for the before image
 * @param afterImage Optional DrawableResource for the after image
 * @param beforeImagePath Optional String path for the before image (loaded via Coil)
 * @param afterImagePath Optional String path for the after image (loaded via Coil)
 * @param initialDragPosition Initial position of the drag handle (defaults to sliderPosition)
 * @param animateToSliderPosition Whether to animate back to sliderPosition after user interaction
 * @param modifier Modifier for the component
 */
@Composable
fun SharedBeforeAfterSlider(
    sliderPosition: Float,
    beforeLabel: UiText,
    afterLabel: UiText,
    beforeColor: Color,
    afterColor: Color,
    beforeImage: DrawableResource? = null,
    afterImage: DrawableResource? = null,
    beforeImagePath: String? = null,
    afterImagePath: String? = null,
    initialDragPosition: Float = sliderPosition,
    animateToSliderPosition: Boolean = true,
    autoRevealDurationMs: Int? = null,
    autoRevealDelayMs: Long = 300L,
    onRevealComplete: (() -> Unit)? = null,
    onAspectRatioDetected: ((Float) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier
) {
    var dragPosition by remember { mutableFloatStateOf(initialDragPosition) }
    var isUserInteracting by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }
    var autoRevealComplete by remember { mutableStateOf(autoRevealDurationMs == null) }

    // Auto-reveal animation: sweeps from initialDragPosition to sliderPosition
    if (autoRevealDurationMs != null && !autoRevealComplete) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(autoRevealDelayMs)
            val startPosition = initialDragPosition
            val endPosition = sliderPosition
            val steps = 60 * (autoRevealDurationMs / 1000f)
            val stepCount = steps.toInt().coerceAtLeast(1)
            val stepDelay = autoRevealDurationMs / stepCount
            for (i in 1..stepCount) {
                val fraction = i.toFloat() / stepCount
                // Ease out cubic for smooth deceleration
                val eased = 1f - (1f - fraction) * (1f - fraction) * (1f - fraction)
                dragPosition = startPosition + (endPosition - startPosition) * eased
                kotlinx.coroutines.delay(stepDelay.toLong())
            }
            dragPosition = endPosition
            autoRevealComplete = true
            onRevealComplete?.invoke()
        }
    }

    LaunchedEffect(sliderPosition, isUserInteracting, lastInteractionTime) {
        if (!isUserInteracting && animateToSliderPosition && autoRevealComplete) {
            val currentTime = getCurrentTimeMillis()
            if (currentTime - lastInteractionTime > 3000) {
                dragPosition = sliderPosition
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        when {
            // Use Coil for path-based images
            beforeImagePath != null && afterImagePath != null -> {
                CoilImageSlider(
                    beforeImagePath = beforeImagePath,
                    afterImagePath = afterImagePath,
                    beforeLabel = beforeLabel.asString(),
                    afterLabel = afterLabel.asString(),
                    dragPosition = dragPosition,
                    onAspectRatioDetected = onAspectRatioDetected,
                    contentScale = contentScale
                )
            }
            // Use painterResource for DrawableResource images
            beforeImage != null && afterImage != null -> {
                Image(
                    painter = painterResource(afterImage),
                    contentDescription = afterLabel.asString(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(beforeImage),
                        contentDescription = beforeLabel.asString(),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(SliderClipShape(dragPosition)),
                        contentScale = contentScale
                    )
                }
            }
            // Fallback to colored canvas
            else -> {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawBeforeAfterSliderWithHandle(
                        sliderPosition = dragPosition,
                        beforeColor = beforeColor,
                        afterColor = afterColor
                    )
                }
            }
        }

        // Slider handle and line (interactive)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isUserInteracting = true
                        },
                        onDragEnd = {
                            isUserInteracting = false
                            lastInteractionTime = getCurrentTimeMillis()
                        }
                    ) { change, _ ->
                        val newPosition = change.position.x / size.width
                        dragPosition = min(1f, max(0f, newPosition))
                    }
                }
        ) {
            val sliderX = size.width * dragPosition
            val handleRadius = 12.dp.toPx()
            val centerY = size.height / 2f

            drawLine(
                color = Color.White,
                start = Offset(sliderX, 0f),
                end = Offset(sliderX, size.height),
                strokeWidth = 2.dp.toPx()
            )

            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = handleRadius + 2.dp.toPx(),
                center = Offset(sliderX, centerY)
            )

            drawCircle(
                color = Color.White,
                radius = handleRadius,
                center = Offset(sliderX, centerY)
            )
        }

        // Labels — show beforeLabel when slider near right (before image fills),
        // show afterLabel when slider near left (after image fills)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            if (dragPosition > 0.8f) {
                Text(
                    text = beforeLabel.asString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            if (dragPosition < 0.2f) {
                Text(
                    text = afterLabel.asString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun CoilImageSlider(
    beforeImagePath: String,
    afterImagePath: String,
    beforeLabel: String,
    afterLabel: String,
    dragPosition: Float,
    onAspectRatioDetected: ((Float) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    // After image (full width)
    SubcomposeAsyncImage(
        model = afterImagePath.toUri(),
        contentDescription = afterLabel,
        modifier = Modifier.fillMaxSize(),
        contentScale = contentScale,
        onSuccess = { state ->
            val width = state.result.image.width.toFloat()
            val height = state.result.image.height.toFloat()
            if (height > 0) {
                onAspectRatioDetected?.invoke(width / height)
            }
        },
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.error_image_load_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )

    // Before image (clipped by slider position)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SubcomposeAsyncImage(
            model = beforeImagePath.toUri(),
            contentDescription = beforeLabel,
            modifier = Modifier
                .fillMaxSize()
                .clip(SliderClipShape(dragPosition)),
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.error_image_load_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
    }
}

private class SliderClipShape(private val sliderPosition: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val sliderX = size.width * sliderPosition
        return Outline.Rectangle(
            Rect(0f, 0f, sliderX, size.height)
        )
    }
}

private fun DrawScope.drawBeforeAfterSliderWithHandle(
    sliderPosition: Float,
    beforeColor: Color,
    afterColor: Color
) {
    val sliderX = size.width * sliderPosition

    drawRect(
        color = beforeColor.copy(alpha = 0.9f),
        topLeft = Offset.Zero,
        size = Size(sliderX, size.height)
    )

    drawRect(
        color = afterColor,
        topLeft = Offset(sliderX, 0f),
        size = Size(size.width - sliderX, size.height)
    )
}