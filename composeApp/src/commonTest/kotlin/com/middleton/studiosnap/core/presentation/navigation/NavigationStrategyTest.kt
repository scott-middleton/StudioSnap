package com.middleton.studiosnap.core.presentation.navigation

import com.middleton.studiosnap.feature.onboarding.presentation.navigation.OnboardingNavigationAction
import com.middleton.studiosnap.feature.splash.presentation.navigation.SplashNavigationAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationStrategyTest {
    
    @Test
    fun `SharedNavigationStrategy should call SharedNavigationManager with correct command for onboarding`() {
        // Given
        val fakeNavigationHandler = FakeNavigationHandler()
        val sharedNavigationManager = SharedNavigationManager(fakeNavigationHandler)
        val strategy = SharedNavigationStrategy<OnboardingNavigationAction>(sharedNavigationManager)
        
        // When
        strategy.navigate(OnboardingNavigationAction.OnGetStarted)
        
        // Then
        assertEquals(1, fakeNavigationHandler.capturedCommands.size)
        assertTrue(fakeNavigationHandler.capturedCommands[0] is NavigationCommand.NavigateAndClearStack)
        val navigateCommand = fakeNavigationHandler.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        assertEquals(Route.MainRestoreScreen, navigateCommand.destination)
    }
    
    @Test
    fun `SharedNavigationStrategy should handle back navigation for onboarding`() {
        // Given
        val fakeNavigationHandler = FakeNavigationHandler()
        val sharedNavigationManager = SharedNavigationManager(fakeNavigationHandler)
        val strategy = SharedNavigationStrategy<OnboardingNavigationAction>(sharedNavigationManager)
        
        // When
        strategy.navigate(OnboardingNavigationAction.OnBack)
        
        // Then
        assertEquals(1, fakeNavigationHandler.capturedCommands.size)
        assertTrue(fakeNavigationHandler.capturedCommands[0] is NavigationCommand.NavigateBack)
    }
    
    @Test
    fun `SharedNavigationStrategy should handle multiple navigation calls for onboarding`() {
        // Given
        val fakeNavigationHandler = FakeNavigationHandler()
        val sharedNavigationManager = SharedNavigationManager(fakeNavigationHandler)
        val strategy = SharedNavigationStrategy<OnboardingNavigationAction>(sharedNavigationManager)
        
        // When
        strategy.navigate(OnboardingNavigationAction.OnGetStarted)
        strategy.navigate(OnboardingNavigationAction.OnBack)
        strategy.navigate(OnboardingNavigationAction.OnGetStarted)
        
        // Then
        assertEquals(3, fakeNavigationHandler.capturedCommands.size)
        assertTrue(fakeNavigationHandler.capturedCommands[0] is NavigationCommand.NavigateAndClearStack)
        assertTrue(fakeNavigationHandler.capturedCommands[1] is NavigationCommand.NavigateBack)
        assertTrue(fakeNavigationHandler.capturedCommands[2] is NavigationCommand.NavigateAndClearStack)
    }
    
    @Test
    fun `SharedNavigationStrategy should work with splash navigation actions`() {
        // Given
        val fakeNavigationHandler = FakeNavigationHandler()
        val sharedNavigationManager = SharedNavigationManager(fakeNavigationHandler)
        val strategy = SharedNavigationStrategy<SplashNavigationAction>(sharedNavigationManager)
        
        // When
        strategy.navigate(SplashNavigationAction.NavigateToOnboarding)
        
        // Then
        assertEquals(1, fakeNavigationHandler.capturedCommands.size)
        assertTrue(fakeNavigationHandler.capturedCommands[0] is NavigationCommand.NavigateAndClearStack)
        val navigateCommand = fakeNavigationHandler.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        assertEquals(Route.OnboardingCarousel, navigateCommand.destination)
    }
    
    @Test
    fun `SharedNavigationStrategy should work with different action types using same pattern`() {
        // Given
        val fakeNavigationHandler1 = FakeNavigationHandler()
        val fakeNavigationHandler2 = FakeNavigationHandler()
        val sharedNavigationManager1 = SharedNavigationManager(fakeNavigationHandler1)
        val sharedNavigationManager2 = SharedNavigationManager(fakeNavigationHandler2)
        val onboardingStrategy = SharedNavigationStrategy<OnboardingNavigationAction>(sharedNavigationManager1)
        val splashStrategy = SharedNavigationStrategy<SplashNavigationAction>(sharedNavigationManager2)
        
        // When
        onboardingStrategy.navigate(OnboardingNavigationAction.OnGetStarted)
        splashStrategy.navigate(SplashNavigationAction.NavigateToOnboarding)
        
        // Then
        assertEquals(1, fakeNavigationHandler1.capturedCommands.size)
        assertEquals(1, fakeNavigationHandler2.capturedCommands.size)
        
        val onboardingCommand = fakeNavigationHandler1.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        val splashCommand = fakeNavigationHandler2.capturedCommands[0] as NavigationCommand.NavigateAndClearStack

        assertEquals(Route.MainRestoreScreen, onboardingCommand.destination)
        assertEquals(Route.OnboardingCarousel, splashCommand.destination)
    }
    
    @Test
    fun `FakeNavigationStrategy should track navigation actions correctly`() {
        // Given
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()
        
        // When
        fakeStrategy.navigate(OnboardingNavigationAction.OnGetStarted)
        fakeStrategy.navigate(OnboardingNavigationAction.OnBack)
        
        // Then
        assertEquals(2, fakeStrategy.getNavigationCount())
        assertTrue(fakeStrategy.hasNavigated())
        assertTrue(fakeStrategy.hasNavigatedTo(OnboardingNavigationAction.OnGetStarted))
        assertTrue(fakeStrategy.hasNavigatedTo(OnboardingNavigationAction.OnBack))
        assertEquals(OnboardingNavigationAction.OnBack, fakeStrategy.getLastNavigatedAction())
    }
    
    @Test
    fun `FakeNavigationStrategy should clear navigation history`() {
        // Given
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()
        fakeStrategy.navigate(OnboardingNavigationAction.OnGetStarted)
        
        // When
        fakeStrategy.clear()
        
        // Then
        assertEquals(0, fakeStrategy.getNavigationCount())
        assertEquals(null, fakeStrategy.getLastNavigatedAction())
    }
    
    @Test
    fun `FakeNavigationStrategy should throw error when configured`() {
        // Given
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()
        fakeStrategy.shouldThrowError = true
        fakeStrategy.errorMessage = "Test navigation error"
        
        // When/Then
        try {
            fakeStrategy.navigate(OnboardingNavigationAction.OnGetStarted)
            throw AssertionError("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertEquals("Test navigation error", e.message)
        }
    }
}