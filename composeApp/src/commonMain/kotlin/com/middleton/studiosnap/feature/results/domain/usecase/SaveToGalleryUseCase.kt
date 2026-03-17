package com.middleton.studiosnap.feature.results.domain.usecase

import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.ErrorReporter

/**
 * Saves a generated image to the device photo gallery.
 * No credit gating — all results are full quality.
 */
class SaveToGalleryUseCase(
    private val galleryRepository: GalleryRepository,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(localFilePath: String, displayName: String): Result<String> {
        return galleryRepository.saveImage(localFilePath, displayName)
            .onFailure { errorReporter.recordException(it) }
    }
}
