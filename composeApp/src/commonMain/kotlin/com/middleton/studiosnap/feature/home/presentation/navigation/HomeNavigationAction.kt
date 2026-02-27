package com.middleton.studiosnap.feature.home.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class HomeNavigationAction : NavigationAction {

    data class GoToProcessing(val generationConfigId: String) : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(
            Route.Processing(generationConfigId)
        )
    }

    data object GoToSettings : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(Route.Settings)
    }

    data object GoToHistory : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(Route.History)
    }

    data object GoToCreditStore : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(Route.CreditStore)
    }
}
