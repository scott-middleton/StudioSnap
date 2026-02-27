package com.middleton.studiosnap.core.presentation.util

/**
 * Saves an image to the device's photo gallery.
 * @param filePath The path to the image file to save
 * @param fileName The display name for the saved image (without extension)
 * @return Result indicating success or failure with error message
 */
expect suspend fun saveImageToGallery(filePath: String, fileName: String): Result<Unit>
