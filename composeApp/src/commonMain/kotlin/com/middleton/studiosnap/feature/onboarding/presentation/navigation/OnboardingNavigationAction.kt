package com.middleton.studiosnap.feature.onboarding.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class OnboardingNavigationAction : NavigationAction {

    data object GoToHome : OnboardingNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateAndClearStack(Route.Home)
    }
}
