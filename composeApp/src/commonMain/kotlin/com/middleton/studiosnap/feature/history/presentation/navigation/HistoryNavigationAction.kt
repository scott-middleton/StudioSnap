package com.middleton.studiosnap.feature.history.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class HistoryNavigationAction : NavigationAction {

    data class GoToSessionDetail(val sessionId: String) : HistoryNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(
            Route.SessionDetail(sessionId)
        )
    }

    data object GoBack : HistoryNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }
}
