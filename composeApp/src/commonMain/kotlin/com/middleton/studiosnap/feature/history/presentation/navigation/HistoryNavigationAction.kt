package com.middleton.studiosnap.feature.history.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class HistoryNavigationAction : NavigationAction {

    data class GoToResultDetail(val resultId: String) : HistoryNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(
            Route.ResultDetail(resultId)
        )
    }

    data object GoBack : HistoryNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }
}
