package com.middleton.studiosnap.core.presentation.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun Modifier.modernShadow(
    elevation: Dp,
    shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium),
    ambientColor: Color = Color.Black.copy(alpha = 0.08f),
    spotColor: Color = Color.Black.copy(alpha = 0.15f)
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = true,
    ambientColor = ambientColor,
    spotColor = spotColor
)

@Composable
fun Modifier.interactiveShadow(
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium),
    ambientColor: Color = Color.Black.copy(alpha = 0.08f),
    spotColor: Color = Color.Black.copy(alpha = 0.15f)
): Modifier {
    val targetElevation = when {
        isPressed -> ImageCloneAiElevation.Level1
        isSelected -> ImageCloneAiElevation.Level3
        else -> ImageCloneAiElevation.Level2
    }
    
    val animatedElevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = tween(150)
    )
    
    return modernShadow(
        elevation = animatedElevation,
        shape = shape,
        ambientColor = ambientColor,
        spotColor = spotColor
    )
}

@Composable
fun Modifier.pressableShadow(
    interactionSource: MutableInteractionSource,
    baseElevation: Dp = ImageCloneAiElevation.Level2,
    pressedElevation: Dp = ImageCloneAiElevation.Level1,
    shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium),
    ambientColor: Color = Color.Black.copy(alpha = 0.08f),
    spotColor: Color = Color.Black.copy(alpha = 0.15f)
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) pressedElevation else baseElevation,
        animationSpec = tween(150)
    )
    
    return modernShadow(
        elevation = animatedElevation,
        shape = shape,
        ambientColor = ambientColor,
        spotColor = spotColor
    )
}

object ModernShadows {
    @Composable
    fun Modifier.subtle(shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium)) = 
        modernShadow(ImageCloneAiElevation.Level1, shape)
    
    @Composable
    fun Modifier.medium(shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium)) = 
        modernShadow(ImageCloneAiElevation.Level2, shape)
    
    @Composable
    fun Modifier.strong(shape: Shape = RoundedCornerShape(ImageCloneAiCorners.Medium)) = 
        modernShadow(ImageCloneAiElevation.Level4, shape)
}