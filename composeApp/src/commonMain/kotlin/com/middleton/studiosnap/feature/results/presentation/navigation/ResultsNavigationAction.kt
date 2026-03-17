package com.middleton.studiosnap.feature.results.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class ResultsNavigationAction : NavigationAction {

    data object GoBack : ResultsNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }

    data object GoToHome : ResultsNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateAndClearStack(Route.Home)
    }
}
