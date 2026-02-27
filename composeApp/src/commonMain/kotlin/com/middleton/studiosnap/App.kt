package com.middleton.studiosnap

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.middleton.studiosnap.core.data.di.coreDataModule
import com.middleton.studiosnap.core.data.di.platformModule
import com.middleton.studiosnap.core.data.di.supabaseModule
import com.middleton.studiosnap.core.presentation.di.coreNavigationModule
import com.middleton.studiosnap.core.presentation.navigation.Route
import com.middleton.studiosnap.core.presentation.theme.StudioSnapTheme
import com.middleton.studiosnap.feature.auth.presentation.di.authModule
import com.middleton.studiosnap.feature.history.presentation.di.historyModule
import com.middleton.studiosnap.feature.home.presentation.di.homeModule
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingCarouselScreen
import com.middleton.studiosnap.feature.onboarding.presentation.di.onboardingModule
import com.middleton.studiosnap.feature.paywall.presentation.di.paywallModule
import com.middleton.studiosnap.feature.processing.presentation.di.processingModule
import com.middleton.studiosnap.feature.results.presentation.di.resultsModule
import com.middleton.studiosnap.feature.settings.presentation.di.settingsModule
import com.middleton.studiosnap.feature.splash.presentation.SplashScreen
import com.middleton.studiosnap.feature.splash.presentation.di.splashModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    val navController = rememberNavController()

    KoinApplication(application = {
        modules(
            platformModule(),
            supabaseModule,
            coreDataModule,
            coreNavigationModule(navController),
            authModule,
            splashModule,
            onboardingModule,
            paywallModule,
            homeModule,
            processingModule,
            resultsModule,
            historyModule,
            settingsModule
        )
    }) {
        StudioSnapTheme {
            NavHost(
                navController = navController,
                startDestination = Route.Splash,
                enterTransition = getAppTransitions().enterTransition,
                exitTransition = getAppTransitions().exitTransition,
                popEnterTransition = getAppTransitions().popEnterTransition,
                popExitTransition = getAppTransitions().popExitTransition
            ) {
                addSplashScreen()
                addOnboardingScreen()
                addStubScreens()
            }
        }
    }
}

private fun NavGraphBuilder.addSplashScreen() {
    composable<Route.Splash>(
        exitTransition = { ExitTransition.None }
    ) {
        SplashScreen()
    }
}

private fun NavGraphBuilder.addOnboardingScreen() {
    composable<Route.Onboarding>(
        enterTransition = { EnterTransition.None }
    ) {
        OnboardingCarouselScreen()
    }
}

/**
 * Stub composable destinations — will be replaced with real screens in Phase 5.
 */
private fun NavGraphBuilder.addStubScreens() {
    composable<Route.Home> { StubScreen("Home") }
    composable<Route.CreditStore> { StubScreen("Credit Store") }
    composable<Route.History> { StubScreen("History") }
    composable<Route.Settings> { StubScreen("Settings") }
}

// TODO: Replace with real screens in Phase 5. Hardcoded strings acceptable in stubs.
@Composable
private fun StubScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name (coming soon)")
    }
}

private fun getAppTransitions() = object {
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(250))
    }
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(200))
    }
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(250))
    }
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(200))
    }
}
