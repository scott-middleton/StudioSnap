package com.middleton.studiosnap.feature.onboarding.domain.model

import androidx.compose.ui.graphics.Color
import com.middleton.studiosnap.core.domain.model.UiText
import org.jetbrains.compose.resources.DrawableResource

data class DemoExample(
    val title: UiText,
    val description: UiText,
    val beforeLabel: UiText,
    val afterLabel: UiText,
    val beforeColor: Color,
    val afterColor: Color,
    val beforeImage: DrawableResource? = null,
    val afterImage: DrawableResource? = null
)