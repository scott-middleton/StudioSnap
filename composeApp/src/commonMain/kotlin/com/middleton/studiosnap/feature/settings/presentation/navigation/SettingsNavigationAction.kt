package com.middleton.studiosnap.feature.settings.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class SettingsNavigationAction : NavigationAction {

    data object GoToCreditStore : SettingsNavigationAction() {
        override val navigationCommand = NavigationCommand.Navigate(Route.CreditStore)
    }

    data object GoBack : SettingsNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }
}
