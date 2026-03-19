package com.middleton.studiosnap.feature.home.presentation.action

import com.middleton.studiosnap.feature.home.domain.model.ExportFormat

sealed interface HomeUiAction {
    data object OnAddPhotosClicked : HomeUiAction
    data object OnPhotoPickerCancelled : HomeUiAction
    data class OnPhotosSelected(val uris: List<String>) : HomeUiAction
    data class OnPhotoRemoved(val photoId: String) : HomeUiAction
    data class OnStyleSelected(val styleId: String) : HomeUiAction
    data class OnShadowToggled(val enabled: Boolean) : HomeUiAction
    data class OnReflectionToggled(val enabled: Boolean) : HomeUiAction
    data class OnExportFormatSelected(val format: ExportFormat) : HomeUiAction
    data object OnStylePickerClicked : HomeUiAction
    data object OnGenerateClicked : HomeUiAction
    data object OnSettingsClicked : HomeUiAction
    data object OnHistoryClicked : HomeUiAction
    data object OnViewAllHistoryClicked : HomeUiAction
    data class OnRecentGenerationClicked(val generationId: String) : HomeUiAction
    data object OnCreditBalanceClicked : HomeUiAction
    data object OnErrorDismissed : HomeUiAction
    data object OnNavigationHandled : HomeUiAction
}
