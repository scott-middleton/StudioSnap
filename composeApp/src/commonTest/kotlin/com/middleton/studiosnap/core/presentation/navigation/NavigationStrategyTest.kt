package com.middleton.studiosnap.core.presentation.navigation

import com.middleton.studiosnap.feature.onboarding.presentation.navigation.OnboardingNavigationAction
import com.middleton.studiosnap.feature.splash.presentation.navigation.SplashNavigationAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationStrategyTest {

    @Test
    fun `SharedNavigationStrategy calls handler with correct command for onboarding`() {
        val fakeHandler = FakeNavigationHandler()
        val manager = SharedNavigationManager(fakeHandler)
        val strategy = SharedNavigationStrategy<OnboardingNavigationAction>(manager)

        strategy.navigate(OnboardingNavigationAction.GoToHome)

        assertEquals(1, fakeHandler.capturedCommands.size)
        assertTrue(fakeHandler.capturedCommands[0] is NavigationCommand.NavigateAndClearStack)
        val cmd = fakeHandler.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        assertEquals(Route.Home, cmd.destination)
    }

    @Test
    fun `SharedNavigationStrategy handles multiple calls`() {
        val fakeHandler = FakeNavigationHandler()
        val manager = SharedNavigationManager(fakeHandler)
        val strategy = SharedNavigationStrategy<OnboardingNavigationAction>(manager)

        strategy.navigate(OnboardingNavigationAction.GoToHome)
        strategy.navigate(OnboardingNavigationAction.GoToHome)

        assertEquals(2, fakeHandler.capturedCommands.size)
    }

    @Test
    fun `SharedNavigationStrategy works with splash actions`() {
        val fakeHandler = FakeNavigationHandler()
        val manager = SharedNavigationManager(fakeHandler)
        val strategy = SharedNavigationStrategy<SplashNavigationAction>(manager)

        strategy.navigate(SplashNavigationAction.GoToOnboarding)

        assertEquals(1, fakeHandler.capturedCommands.size)
        val cmd = fakeHandler.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        assertEquals(Route.Onboarding, cmd.destination)
    }

    @Test
    fun `splash GoToHome navigates to Home`() {
        val fakeHandler = FakeNavigationHandler()
        val manager = SharedNavigationManager(fakeHandler)
        val strategy = SharedNavigationStrategy<SplashNavigationAction>(manager)

        strategy.navigate(SplashNavigationAction.GoToHome)

        val cmd = fakeHandler.capturedCommands[0] as NavigationCommand.NavigateAndClearStack
        assertEquals(Route.Home, cmd.destination)
    }

    @Test
    fun `different strategy types are independent`() {
        val handler1 = FakeNavigationHandler()
        val handler2 = FakeNavigationHandler()
        val onboardingStrategy = SharedNavigationStrategy<OnboardingNavigationAction>(SharedNavigationManager(handler1))
        val splashStrategy = SharedNavigationStrategy<SplashNavigationAction>(SharedNavigationManager(handler2))

        onboardingStrategy.navigate(OnboardingNavigationAction.GoToHome)
        splashStrategy.navigate(SplashNavigationAction.GoToOnboarding)

        assertEquals(1, handler1.capturedCommands.size)
        assertEquals(1, handler2.capturedCommands.size)
        assertEquals(Route.Home, (handler1.capturedCommands[0] as NavigationCommand.NavigateAndClearStack).destination)
        assertEquals(Route.Onboarding, (handler2.capturedCommands[0] as NavigationCommand.NavigateAndClearStack).destination)
    }

    @Test
    fun `FakeNavigationStrategy tracks actions correctly`() {
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()

        fakeStrategy.navigate(OnboardingNavigationAction.GoToHome)

        assertEquals(1, fakeStrategy.getNavigationCount())
        assertTrue(fakeStrategy.hasNavigated())
        assertTrue(fakeStrategy.hasNavigatedTo(OnboardingNavigationAction.GoToHome))
        assertEquals(OnboardingNavigationAction.GoToHome, fakeStrategy.getLastNavigatedAction())
    }

    @Test
    fun `FakeNavigationStrategy clears history`() {
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()
        fakeStrategy.navigate(OnboardingNavigationAction.GoToHome)

        fakeStrategy.clear()

        assertEquals(0, fakeStrategy.getNavigationCount())
        assertEquals(null, fakeStrategy.getLastNavigatedAction())
    }

    @Test
    fun `FakeNavigationStrategy throws error when configured`() {
        val fakeStrategy = FakeNavigationStrategy<OnboardingNavigationAction>()
        fakeStrategy.shouldThrowError = true
        fakeStrategy.errorMessage = "Test navigation error"

        try {
            fakeStrategy.navigate(OnboardingNavigationAction.GoToHome)
            throw AssertionError("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Test navigation error", e.message)
        }
    }
}
