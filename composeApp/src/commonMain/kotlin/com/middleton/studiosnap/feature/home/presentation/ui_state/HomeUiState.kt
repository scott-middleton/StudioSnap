package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style

data class HomeUiState(
    val photos: List<ProductPhoto> = emptyList(),
    val selectedStyle: Style? = null,
    val showGalleryPicker: Boolean = false,
    val shadow: Boolean = false,
    val reflection: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.DEFAULT,
    val creditBalance: Int = 0,
    val isSignedIn: Boolean = false,
    val error: HomeError? = null
) {
    val canGenerate: Boolean
        get() = photos.isNotEmpty() && selectedStyle != null

    val hasPhotos: Boolean
        get() = photos.isNotEmpty()

    val photoCount: Int
        get() = photos.size

    companion object {
        const val MAX_PHOTOS = 10
    }
}

sealed interface HomeError {
    data object TooManyPhotos : HomeError
    data object GenerationFailed : HomeError
}
