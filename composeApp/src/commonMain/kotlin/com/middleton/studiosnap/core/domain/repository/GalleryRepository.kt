package com.middleton.studiosnap.core.domain.repository

/**
 * Abstraction for device photo gallery operations.
 * Platform implementations handle MediaStore (Android) and PHPhotoLibrary (iOS).
 */
interface GalleryRepository {

    /**
     * Saves an image to the device gallery.
     * @param filePath Local file path of the image to save
     * @param displayName Display name for the gallery entry (without extension)
     * @return The gallery URI string for the saved image
     */
    suspend fun saveImage(filePath: String, displayName: String): Result<String>

    /**
     * Deletes an image from the device gallery.
     * @param galleryUri The URI string returned from [saveImage]
     * @return Success or failure
     */
    suspend fun deleteImage(galleryUri: String): Result<Unit>

    /**
     * Checks whether an image still exists in the device gallery.
     * @param galleryUri The URI string returned from [saveImage]
     * @return true if the image exists, false if deleted or unavailable
     */
    suspend fun imageExists(galleryUri: String): Boolean
}
