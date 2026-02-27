package com.middleton.studiosnap.core.presentation.navigation

class SharedNavigationStrategy<T : NavigationAction>(
    private val sharedNavigationManager: SharedNavigationManager
) : NavigationStrategy<T> {
    override fun navigate(action: T) {
        sharedNavigationManager.navigate(action.navigationCommand)
    }
}