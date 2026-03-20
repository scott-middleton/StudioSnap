package com.middleton.studiosnap.core.presentation.navigation

import androidx.navigation.NavHostController

interface NavigationHandler {
    fun handleNavigation(command: NavigationCommand)
}

/**
 * NavigationHandler backed by a NavHostController.
 * The controller is injected lazily via [setNavController] rather than
 * via constructor so that this singleton can be registered in Koin at
 * Application start (before the Compose NavHost is created).
 *
 * Both setNavController and handleNavigation are called on the main thread.
 */
class NavControllerNavigationHandler : NavigationHandler {
    private var navController: NavHostController? = null

    fun setNavController(controller: NavHostController) {
        navController = controller
    }

    override fun handleNavigation(command: NavigationCommand) {
        val navController = navController ?: return
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
