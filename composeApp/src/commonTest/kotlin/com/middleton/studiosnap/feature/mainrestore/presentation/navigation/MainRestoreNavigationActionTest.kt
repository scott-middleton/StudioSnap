package com.middleton.studiosnap.feature.mainrestore.presentation.navigation

import com.middleton.studiosnap.core.presentation.navigation.NavigationCommand
import com.middleton.studiosnap.core.presentation.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainRestoreNavigationActionTest {

    @Test
    fun `NavigateToProcessing should navigate to ProcessingScreen with imageUri`() {
        val action = MainRestoreNavigationAction.NavigateToProcessing(
            imageUri = "content://test/image.jpg"
        )

        val command = action.navigationCommand

        assertTrue(command is NavigationCommand.Navigate)
        val destination = command.destination
        assertTrue(destination is Route.ProcessingScreen)
        assertEquals("content://test/image.jpg", destination.imageUri)
        assertFalse(destination.isFreeRestoration)
    }

    @Test
    fun `NavigateToProcessing with free restoration should set isFreeRestoration true`() {
        val action = MainRestoreNavigationAction.NavigateToProcessing(
            imageUri = "content://test/image.jpg",
            isFreeRestoration = true
        )

        val command = action.navigationCommand as NavigationCommand.Navigate
        val destination = command.destination as Route.ProcessingScreen
        assertTrue(destination.isFreeRestoration)
    }

    @Test
    fun `NavigateToTokenPurchase should navigate to correct route`() {
        val action = MainRestoreNavigationAction.NavigateToTokenPurchase

        val command = action.navigationCommand

        assertTrue(command is NavigationCommand.Navigate)
        assertEquals(Route.PaywallScreen, command.destination)
    }

    @Test
    fun `NavigateToRestorationExamples should pass initialType`() {
        val action = MainRestoreNavigationAction.NavigateToRestorationExamples(
            initialType = "PORTRAIT"
        )

        val command = action.navigationCommand as NavigationCommand.Navigate
        val destination = command.destination as Route.RestorationExamplesScreen
        assertEquals("PORTRAIT", destination.initialType)
    }

    @Test
    fun `NavigateToRestorationExamples with null type should pass null`() {
        val action = MainRestoreNavigationAction.NavigateToRestorationExamples()

        val command = action.navigationCommand as NavigationCommand.Navigate
        val destination = command.destination as Route.RestorationExamplesScreen
        assertEquals(null, destination.initialType)
    }
}

class ProcessingNavigationActionTest {

    @Test
    fun `NavigateToResult should pop current and navigate to ResultScreen`() {
        val action = ProcessingNavigationAction.NavigateToResult
        val command = action.navigationCommand
        assertTrue(command is NavigationCommand.NavigateAndPopCurrent)
        assertEquals(Route.ResultScreen, command.destination)
    }

    @Test
    fun `NavigateBack should navigate back`() {
        val action = ProcessingNavigationAction.NavigateBack
        val command = action.navigationCommand
        assertTrue(command is NavigationCommand.NavigateBack)
    }
}

class ResultNavigationActionTest {

    @Test
    fun `RestoreAnother should clear stack and navigate to MainRestore`() {
        val action = ResultNavigationAction.RestoreAnother
        val command = action.navigationCommand
        assertTrue(command is NavigationCommand.NavigateAndClearStack)
        assertEquals(Route.MainRestoreScreen, command.destination)
    }

    @Test
    fun `UnlockFullQuality should pop current and navigate to Paywall`() {
        val action = ResultNavigationAction.UnlockFullQuality
        val command = action.navigationCommand
        assertTrue(command is NavigationCommand.NavigateAndPopCurrent)
        assertEquals(Route.PaywallScreen, command.destination)
    }
}
