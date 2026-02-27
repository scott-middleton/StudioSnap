package com.middleton.studiosnap.feature.onboarding.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingNavigationActionTest {
    
    @Test
    fun `OnGetStarted should navigate to MainRestoreScreen`() {
        // Given
        val action = OnboardingNavigationAction.OnGetStarted
        
        // When
        val command = action.navigationCommand
        
        // Then
        assertTrue(command is NavigationCommand.NavigateAndClearStack)
        assertEquals(Route.MainRestoreScreen, command.destination)
    }
    
    @Test
    fun `OnBack should navigate back`() {
        // Given
        val action = OnboardingNavigationAction.OnBack
        
        // When
        val command = action.navigationCommand
        
        // Then
        assertTrue(command is NavigationCommand.NavigateBack)
    }
    
    @Test
    fun `OnGetStarted should be consistent across multiple calls`() {
        // Given
        val action1 = OnboardingNavigationAction.OnGetStarted
        val action2 = OnboardingNavigationAction.OnGetStarted
        
        // When
        val command1 = action1.navigationCommand
        val command2 = action2.navigationCommand
        
        // Then
        assertEquals(command1, command2)
        assertTrue(command1 is NavigationCommand.NavigateAndClearStack)
        assertTrue(command2 is NavigationCommand.NavigateAndClearStack)
        assertEquals(
            (command1 as NavigationCommand.NavigateAndClearStack).destination,
            (command2 as NavigationCommand.NavigateAndClearStack).destination
        )
    }
    
    @Test
    fun `OnBack should be consistent across multiple calls`() {
        // Given
        val action1 = OnboardingNavigationAction.OnBack
        val action2 = OnboardingNavigationAction.OnBack
        
        // When
        val command1 = action1.navigationCommand
        val command2 = action2.navigationCommand
        
        // Then
        assertEquals(command1, command2)
        assertEquals(NavigationCommand.NavigateBack, command1)
        assertEquals(NavigationCommand.NavigateBack, command2)
    }
    
    @Test
    fun `all OnboardingNavigationAction types should implement NavigationAction`() {
        // Given
        val onGetStarted = OnboardingNavigationAction.OnGetStarted
        val onBack = OnboardingNavigationAction.OnBack
        
        // When/Then - Should compile and not throw
        val getStartedCommand = onGetStarted.navigationCommand
        val backCommand = onBack.navigationCommand
        
        // Verify they produce valid navigation commands
        assertTrue(getStartedCommand is NavigationCommand)
        assertTrue(backCommand is NavigationCommand)
    }
    
    @Test
    fun `OnGetStarted should navigate to correct route type`() {
        // Given
        val action = OnboardingNavigationAction.OnGetStarted
        
        // When
        val command = action.navigationCommand as NavigationCommand.NavigateAndClearStack
        
        // Then
        // Verify it's navigating to a main app screen, not back to onboarding
        assertTrue(command.destination is Route.MainRestoreScreen)
        assertEquals(Route.MainRestoreScreen, command.destination)
    }
}