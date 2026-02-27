package com.middleton.studiosnap.feature.paywall.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed interface PaywallNavigationAction : NavigationAction {
    data object PurchaseComplete : PaywallNavigationAction {
        override val navigationCommand: NavigationCommand
            get() = NavigationCommand.NavigateAndClearStack(Route.Home)
    }

    data object Dismiss : PaywallNavigationAction {
        override val navigationCommand: NavigationCommand
            get() = NavigationCommand.NavigateBack
    }

    data object NavigateBack : PaywallNavigationAction {
        override val navigationCommand: NavigationCommand
            get() = NavigationCommand.NavigateBack
    }
}
