package com.middleton.studiosnap.core.data.database

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository

/**
 * Maps between [GenerationEntity] (Room) and [GenerationResult.Success] (domain).
 * Requires [StyleRepository] to reconstruct the full [Style] from its ID.
 */
fun GenerationEntity.toDomainModel(styleRepository: StyleRepository): GenerationResult.Success {
    val style = styleRepository.getStyleById(styleId)
        ?: Style(
            id = styleId,
            nameKey = styleName,
            categories = emptySet(),
            thumbnailResName = "",
            kontextPrompt = ""
        )

    return GenerationResult.Success(
        generationId = id,
        inputPhoto = ProductPhoto(id = id, localUri = inputPhotoUri),
        watermarkedPreviewUri = watermarkedUri,
        fullResUrl = fullResUrl,
        fullResUri = fullResLocalUri,
        style = style,
        createdAt = createdAt,
        imageWidth = imageWidth,
        imageHeight = imageHeight
    )
}

fun GenerationResult.Success.toEntity(): GenerationEntity {
    return GenerationEntity(
        id = generationId,
        inputPhotoUri = inputPhoto.localUri,
        styleId = style.id,
        styleName = style.nameKey,
        watermarkedUri = watermarkedPreviewUri,
        fullResUrl = fullResUrl,
        fullResLocalUri = fullResUri,
        isPurchased = fullResUri != null,
        shadow = false,
        reflection = false,
        exportFormat = "jpg",
        createdAt = createdAt,
        imageWidth = imageWidth,
        imageHeight = imageHeight
    )
}
