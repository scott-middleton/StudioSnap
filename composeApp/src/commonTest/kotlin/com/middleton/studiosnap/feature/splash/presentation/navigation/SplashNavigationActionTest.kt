package com.middleton.studiosnap.feature.splash.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashNavigationActionTest {

    @Test
    fun `NavigateToOnboarding should navigate to OnboardingCarousel and clear stack`() {
        // Given
        val action = SplashNavigationAction.NavigateToOnboarding

        // When
        val command = action.navigationCommand

        // Then
        assertTrue(command is NavigationCommand.NavigateAndClearStack)
        assertEquals(Route.OnboardingCarousel, command.destination)
    }

    @Test
    fun `NavigateToOnboarding should be consistent across multiple calls`() {
        // Given
        val action1 = SplashNavigationAction.NavigateToOnboarding
        val action2 = SplashNavigationAction.NavigateToOnboarding

        // When
        val command1 = action1.navigationCommand
        val command2 = action2.navigationCommand

        // Then
        assertEquals(command1, command2)
        assertTrue(command1 is NavigationCommand.NavigateAndClearStack)
        assertTrue(command2 is NavigationCommand.NavigateAndClearStack)
        assertEquals(
            command1.destination,
            command2.destination
        )
    }

    @Test
    fun `NavigateToOnboarding should implement NavigationAction`() {
        // Given
        val action = SplashNavigationAction.NavigateToOnboarding

        // When/Then - Should compile and not throw
        val command = action.navigationCommand

        // Verify it produces a valid navigation command
        assertTrue(command is NavigationCommand)
        assertTrue(command is NavigationCommand.NavigateAndClearStack)
    }

    @Test
    fun `NavigateToOnboarding should navigate to correct route type`() {
        // Given
        val action = SplashNavigationAction.NavigateToOnboarding

        // When
        val command = action.navigationCommand as NavigationCommand.NavigateAndClearStack

        // Then
        assertTrue(command.destination is Route.OnboardingCarousel)
        assertEquals(Route.OnboardingCarousel, command.destination)
    }

    @Test
    fun `SplashNavigationAction should only have forward navigation actions`() {
        // Given
        val action = SplashNavigationAction.NavigateToOnboarding

        // When
        val command = action.navigationCommand

        // Then
        // Splash screen should only navigate forward, never back
        assertTrue(command is NavigationCommand.NavigateAndClearStack)
        assertTrue(command !is NavigationCommand.NavigateBack)
    }
}
