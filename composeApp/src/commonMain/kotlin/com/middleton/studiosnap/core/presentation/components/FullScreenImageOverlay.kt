package com.middleton.studiosnap.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import com.middleton.studiosnap.feature.onboarding.presentation.PlatformBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.middleton.studiosnap.core.presentation.util.LockLandscapeOrientation
// coil3.toUri removed

/**
 * Full-screen image overlay with pinch-to-zoom (1x-5x),
 * animated entry, and landscape lock for landscape images.
 */
@Composable
fun FullScreenImageOverlay(
    imagePath: String,
    imageAspectRatio: Float? = null,
    isWatermarked: Boolean = false,
    onDismiss: () -> Unit
) {
    val isLandscapeImage = imageAspectRatio != null && imageAspectRatio > 1f

    if (isLandscapeImage) {
        LockLandscapeOrientation()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val enterScale = remember { Animatable(0.85f) }
    val enterAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAlpha.animateTo(1f, tween(250))
    }
    LaunchedEffect(Unit) {
        enterScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = enterAlpha.value }
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    val maxOffsetX = (newScale - 1f) * size.width / 2f
                    val maxOffsetY = (newScale - 1f) * size.height / 2f
                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    scale = newScale
                    if (newScale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        RestorationImage(
            imagePath = imagePath,
            isWatermarked = isWatermarked,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale * enterScale.value
                    scaleY = scale * enterScale.value
                    translationX = offsetX
                    translationY = offsetY
                },
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        )

    }

    PlatformBackHandler(enabled = true) { onDismiss() }
}
