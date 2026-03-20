package com.middleton.studiosnap.core.presentation.navigation

import kotlinx.serialization.Serializable

sealed class NavigationCommand {
    data class Navigate(val destination: Route) : NavigationCommand()
    data object NavigateBack : NavigationCommand()
    data class NavigateAndClearStack(val destination: Route) : NavigationCommand()
    data class NavigateAndPopCurrent(val destination: Route) : NavigationCommand()
}

@Serializable
sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Processing : Route

    @Serializable
    data object Results : Route

    @Serializable
    data object CreditStore : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class ResultDetail(val resultId: String) : Route

    @Serializable
    data class SessionDetail(val sessionId: String) : Route

    @Serializable
    data class StylePicker(val currentStyleId: String? = null) : Route
}
