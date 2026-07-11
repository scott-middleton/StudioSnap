package com.middleton.studiosnap.feature.home.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class HomeNavigationAction : NavigationAction {

    data object GoToProcessing : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(Route.Processing)
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

    data class GoToStylePicker(val currentStyleIds: List<String>, val maxSelectable: Int) : HomeNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(
            Route.StylePicker(currentStyleIds.joinToString(","), maxSelectable)
        )
    }

    companion object {
        const val STYLE_PICKER_RESULT_KEY = "selected_style_ids"
    }
}
