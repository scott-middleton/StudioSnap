package com.middleton.studiosnap.core.presentation.navigation

interface NavigationStrategy<in T : NavigationAction> {
    fun navigate(action: T)
}