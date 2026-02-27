package com.middleton.studiosnap.core.presentation.navigation

class SharedNavigationManager(
    private val navigationHandler: NavigationHandler
) {
    fun navigate(command: NavigationCommand) {
        navigationHandler.handleNavigation(command)
    }
}