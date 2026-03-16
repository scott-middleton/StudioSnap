package com.middleton.studiosnap.core.data.database

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository

/**
 * Maps between [GenerationEntity] (Room) and [GenerationResult.Success] (domain).
 * Requires [StyleRepository] to reconstruct the full [Style] from its ID.
 */
fun GenerationEntity.toDomainModel(styleRepository: StyleRepository): GenerationResult.Success {
    val style = styleRepository.getStyleById(styleId) ?: run {
        // Style was removed or renamed since this generation was saved.
        // Create a minimal placeholder so history entries remain visible.
        // Style was removed or renamed — use placeholder so history stays visible
        Style(
            id = styleId,
            displayName = UiText.DynamicString(styleName),
            categories = emptySet(),
            thumbnail = null,
            kontextPrompt = ""
        )
    }

    return GenerationResult.Success(
        generationId = id,
        inputPhoto = ProductPhoto(id = id, localUri = inputPhotoUri),
        previewUri = previewUri,
        fullResUrl = fullResUrl,
        fullResUri = fullResLocalUri,
        style = style,
        createdAt = createdAt,
        imageWidth = imageWidth,
        imageHeight = imageHeight
    )
}

/**
 * Converts a domain [GenerationResult.Success] to a Room entity for persistence.
 *
 * Note: [GenerationResult.Success] does not carry shadow, reflection, or exportFormat
 * because those are generation-time configuration stored in [GenerationConfig], not result
 * properties. The entity stores defaults here. If these need to survive the round-trip
 * (e.g. for re-export or history display), add them to [GenerationResult.Success] and
 * thread them through from the generation pipeline.
 */
fun GenerationResult.Success.toEntity(): GenerationEntity {
    return GenerationEntity(
        id = generationId,
        inputPhotoUri = inputPhoto.localUri,
        styleId = style.id,
        styleName = style.id,
        previewUri = previewUri,
        fullResUrl = fullResUrl,
        fullResLocalUri = fullResUri,
        isPurchased = fullResUri != null,
        shadow = false, // TODO: Thread from GenerationConfig if needed for re-export
        reflection = false, // TODO: Thread from GenerationConfig if needed for re-export
        exportFormat = "jpg", // TODO: Thread from GenerationConfig if needed for re-export
        createdAt = createdAt,
        imageWidth = imageWidth,
        imageHeight = imageHeight
    )
}
