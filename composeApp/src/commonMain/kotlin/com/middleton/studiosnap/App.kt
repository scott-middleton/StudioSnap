package com.middleton.studiosnap

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.middleton.studiosnap.core.data.di.coreDataModule
import com.middleton.studiosnap.core.data.di.platformModule
import com.middleton.studiosnap.core.data.di.supabaseModule
import com.middleton.studiosnap.core.presentation.di.coreNavigationModule
import com.middleton.studiosnap.core.presentation.navigation.Route
import com.middleton.studiosnap.core.presentation.theme.ImageCloneAiTheme
import com.middleton.studiosnap.feature.auth.presentation.di.authModule
import com.middleton.studiosnap.feature.mainrestore.presentation.MainRestoreScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.ProcessingScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.RestorationExamplesScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.RestorationDetailScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.RestorationGridScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.ResultScreen
import com.middleton.studiosnap.feature.mainrestore.presentation.di.mainRestoreModule
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingCarouselScreen
import com.middleton.studiosnap.feature.onboarding.presentation.di.onboardingModule
import com.middleton.studiosnap.feature.paywall.presentation.PaywallScreen
import com.middleton.studiosnap.feature.paywall.presentation.di.paywallModule
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
            mainRestoreModule,
            paywallModule
        )
    }) {
        ImageCloneAiTheme {
            NavHost(
                navController = navController,
                startDestination = Route.Splash,
                enterTransition = getAppTransitions().enterTransition,
                exitTransition = getAppTransitions().exitTransition,
                popEnterTransition = getAppTransitions().popEnterTransition,
                popExitTransition = getAppTransitions().popExitTransition
            ) {
                addSplashScreen()
                addOnboardingScreens()
                addPlaceholderScreens(navController)
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

private fun NavGraphBuilder.addOnboardingScreens() {
    composable<Route.OnboardingCarousel>(
        enterTransition = { EnterTransition.None }
    ) {
        OnboardingCarouselScreen()
    }
}

private fun NavGraphBuilder.addPlaceholderScreens(navController: NavController) {
    
    composable<Route.MainRestoreScreen> {
        MainRestoreScreen()
    }
    
    composable<Route.PaywallScreen> {
        PaywallScreen()
    }

    composable<Route.ProcessingScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ProcessingScreen>()
        ProcessingScreen(
            imageUri = route.imageUri,
            isFreeRestoration = route.isFreeRestoration,
            userPrompt = route.userPrompt
        )
    }

    composable<Route.ResultScreen> {
        ResultScreen()
    }
    
    composable<Route.RestorationGridScreen> {
        RestorationGridScreen()
    }

    composable<Route.RestorationDetailScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.RestorationDetailScreen>()
        RestorationDetailScreen(restorationId = route.restorationId)
    }

    composable<Route.RestorationExamplesScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.RestorationExamplesScreen>()
        RestorationExamplesScreen(
            initialType = route.initialType,
            onNavigateBack = {
                navController.popBackStack()
            },
            onTryRestoration = { tier ->
                navController.popBackStack()
            }
        )
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
