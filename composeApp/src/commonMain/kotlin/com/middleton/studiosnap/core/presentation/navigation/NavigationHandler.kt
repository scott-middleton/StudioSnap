package com.middleton.studiosnap.core.presentation.navigation

import androidx.navigation.NavHostController

interface NavigationHandler {
    fun handleNavigation(command: NavigationCommand)
}

class NavControllerNavigationHandler(
    private val navController: NavHostController
) : NavigationHandler {
    override fun handleNavigation(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.Navigate -> {
                navController.navigate(command.destination) {
                    launchSingleTop = true
                }
            }
            is NavigationCommand.NavigateBack -> {
                navController.navigateUp()
            }
            is NavigationCommand.NavigateAndClearStack -> {
                navController.navigate(command.destination) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is NavigationCommand.NavigateAndPopCurrent -> {
                // Pop the current entry first, then navigate.
                // Using popBackStack() is more reliable than string-based popUpTo
                // with type-safe routes, which can have argument placeholders in the route string.
                navController.popBackStack()
                navController.navigate(command.destination) {
                    launchSingleTop = true
                }
            }
        }
    }
}
