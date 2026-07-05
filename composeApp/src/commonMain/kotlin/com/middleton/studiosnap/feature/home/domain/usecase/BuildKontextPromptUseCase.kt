package com.middleton.studiosnap.feature.home.domain.usecase

import com.middleton.studiosnap.feature.home.domain.model.Style

/**
 * Constructs the final Kontext prompt by combining the style's base prompt
 * with optional shadow/reflection modifiers.
 */
class BuildKontextPromptUseCase {

    operator fun invoke(style: Style, shadow: Boolean, reflection: Boolean): String =
        invoke(style.kontextPrompt, shadow, reflection)

    operator fun invoke(basePrompt: String, shadow: Boolean, reflection: Boolean): String {
        val shadowSuffix = if (shadow) SHADOW_SUFFIX else ""
        val reflectionSuffix = if (reflection) REFLECTION_SUFFIX else ""
        return "$basePrompt$shadowSuffix$reflectionSuffix"
    }

    companion object {
        private const val SHADOW_SUFFIX = " Add a natural soft shadow beneath the product."
        private const val REFLECTION_SUFFIX = " Add a subtle reflection of the product on the surface."
    }
}
