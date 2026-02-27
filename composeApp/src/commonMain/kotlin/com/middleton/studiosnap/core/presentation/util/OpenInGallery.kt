package com.middleton.studiosnap.core.presentation.util

/**
 * Opens the given gallery URI in the system photo viewer.
 */
expect suspend fun openInGallery(galleryUri: String): Result<Unit>
