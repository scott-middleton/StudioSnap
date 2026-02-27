package com.middleton.studiosnap.feature.home.domain.repository

import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style

/**
 * Handles the Flux Kontext API interaction for generating styled product photos.
 */
interface GenerationRepository {

    /**
     * Generates a single styled product photo via Flux Kontext.
     * Returns a watermarked preview (free) or full-res (if credits available).
     */
    suspend fun generateImage(
        photo: ProductPhoto,
        style: Style,
        shadow: Boolean,
        reflection: Boolean,
        exportFormat: ExportFormat,
        quality: GenerationQuality
    ): Result<GenerationResult.Success>

    /**
     * Downloads the full-resolution version of a previously generated image.
     * Called after the user pays credits for a watermarked preview.
     */
    suspend fun downloadFullRes(generationId: String): Result<String>
}
