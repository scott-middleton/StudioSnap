package com.middleton.studiosnap.core.presentation.util

/**
 * Opens the native share sheet to share an image file.
 * @param filePath The path to the image file to share
 * @return Result indicating success or failure
 */
expect suspend fun shareImage(filePath: String): Result<Unit>
