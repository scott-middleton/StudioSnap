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

    /**
     * [currentStyleIds] is a comma-joined list of style ids (style ids are snake_case, so
     * commas are safe). A plain String is used instead of List<String> to avoid relying on
     * collection route-arg serialization support across platforms.
     */
    @Serializable
    data class StylePicker(val currentStyleIds: String = "", val maxSelectable: Int = 1) : Route
}
