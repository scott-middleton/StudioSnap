package com.middleton.studiosnap.feature.sessiondetail.presentation.action

sealed interface SessionDetailUiAction {
    data object OnBackClicked : SessionDetailUiAction
    data object OnOpenInGalleryClicked : SessionDetailUiAction
    data object OnDeleteSessionClicked : SessionDetailUiAction
    data object OnDeleteConfirmed : SessionDetailUiAction
    data object OnDeleteDismissed : SessionDetailUiAction
    data object OnNavigationHandled : SessionDetailUiAction
}
