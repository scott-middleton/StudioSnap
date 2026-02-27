package com.middleton.studiosnap.feature.processing.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route

sealed class ProcessingNavigationAction : NavigationAction {

    data class GoToResults(val generationId: String) : ProcessingNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateAndPopCurrent(
            Route.Results(generationId)
        )
    }

    data object GoBack : ProcessingNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }
}
