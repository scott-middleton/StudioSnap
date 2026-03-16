package com.middleton.studiosnap.feature.home.domain.model

/**
 * The outcome of generating a single image.
 * A batch generation produces a list of these.
 */
sealed interface GenerationResult {
    val inputPhoto: ProductPhoto

    data class Success(
        val generationId: String,
        override val inputPhoto: ProductPhoto,
        val previewUri: String,
        val fullResUrl: String? = null,
        val fullResUri: String? = null,
        val style: Style,
        val createdAt: Long,
        val imageWidth: Int = 0,
        val imageHeight: Int = 0
    ) : GenerationResult {
        /** Style display name for UI. Resolves via UiText.asString() in composable context. */
        val styleDisplayName: com.middleton.studiosnap.core.domain.model.UiText get() = style.displayName
    }

    data class Failure(
        override val inputPhoto: ProductPhoto,
        val error: GenerationError
    ) : GenerationResult
}

enum class GenerationError {
    NETWORK,
    TIMEOUT,
    API_ERROR,
    CONTENT_FILTERED,
    UNKNOWN
}
