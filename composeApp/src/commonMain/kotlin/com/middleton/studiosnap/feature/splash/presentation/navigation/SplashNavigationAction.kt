package com.middleton.studiosnap.feature.splash.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class SplashNavigationAction : NavigationAction {

    data object GoToOnboarding : SplashNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateAndClearStack(Route.Onboarding)
    }

    data object GoToHome : SplashNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateAndClearStack(Route.Home)
    }
}
