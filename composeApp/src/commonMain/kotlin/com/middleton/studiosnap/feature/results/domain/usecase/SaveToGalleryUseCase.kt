package com.middleton.studiosnap.feature.results.domain.usecase

import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository

/**
 * Saves a generated image to the device photo gallery and records the returned
 * gallery URI on the generation's history row, so it can later be opened from
 * the gallery (e.g. session detail's open-in-gallery button).
 * No credit gating — all results are full quality.
 */
class SaveToGalleryUseCase(
    private val galleryRepository: GalleryRepository,
    private val historyRepository: HistoryRepository,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(generationId: String, localFilePath: String): Result<String> {
        return galleryRepository.saveImage(localFilePath, "$SAVE_NAME_PREFIX$generationId")
            .onSuccess { galleryUri ->
                // A failed DB write must not fail (or crash) the save — the image is already
                // in the gallery. galleryUri stays null on the row, so a later open-in-gallery
                // self-heals by saving again.
                runCatching { historyRepository.setGalleryUri(generationId, galleryUri) }
                    .onFailure { errorReporter.recordException(it) }
            }
            .onFailure { errorReporter.recordException(it) }
    }

    companion object {
        private const val SAVE_NAME_PREFIX = "studiosnap_"
    }
}
