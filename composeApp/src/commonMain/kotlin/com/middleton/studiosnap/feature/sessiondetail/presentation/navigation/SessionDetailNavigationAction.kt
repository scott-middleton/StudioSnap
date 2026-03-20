package com.middleton.studiosnap.feature.sessiondetail.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationAction
import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand

sealed class SessionDetailNavigationAction : NavigationAction {

    data object GoBack : SessionDetailNavigationAction() {
        override val navigationCommand = NavigationCommand.NavigateBack
    }
}
