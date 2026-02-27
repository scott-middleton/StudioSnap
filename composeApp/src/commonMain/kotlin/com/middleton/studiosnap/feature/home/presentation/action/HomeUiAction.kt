package com.middleton.studiosnap.feature.home.presentation.action

import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

sealed interface HomeUiAction {
    data class OnPhotosSelected(val uris: List<String>) : HomeUiAction
    data class OnPhotoRemoved(val photoId: String) : HomeUiAction
    data class OnStyleSelected(val styleId: String) : HomeUiAction
    data class OnCategorySelected(val category: StyleCategory) : HomeUiAction
    data class OnShadowToggled(val enabled: Boolean) : HomeUiAction
    data class OnReflectionToggled(val enabled: Boolean) : HomeUiAction
    data class OnExportFormatSelected(val format: ExportFormat) : HomeUiAction
    data object OnGenerateClicked : HomeUiAction
    data object OnSettingsClicked : HomeUiAction
    data object OnHistoryClicked : HomeUiAction
    data object OnCreditBalanceClicked : HomeUiAction
    data object OnErrorDismissed : HomeUiAction
}
