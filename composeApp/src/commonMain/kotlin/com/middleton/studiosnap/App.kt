package com.middleton.studiosnap

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
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
import com.middleton.studiosnap.feature.history.presentation.HistoryScreen
import com.middleton.studiosnap.feature.history.presentation.di.historyModule
import androidx.compose.runtime.LaunchedEffect
import com.middleton.studiosnap.feature.home.presentation.HomeScreen
import com.middleton.studiosnap.feature.home.presentation.StylePickerScreen
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.di.homeModule
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingCarouselScreen
import com.middleton.studiosnap.feature.onboarding.presentation.di.onboardingModule
import com.middleton.studiosnap.feature.paywall.presentation.PaywallScreen
import com.middleton.studiosnap.feature.paywall.presentation.di.paywallModule
import com.middleton.studiosnap.feature.processing.presentation.ProcessingScreen
import com.middleton.studiosnap.feature.processing.presentation.di.processingModule
import com.middleton.studiosnap.feature.results.presentation.ResultsScreen
import com.middleton.studiosnap.feature.results.presentation.di.resultsModule
import com.middleton.studiosnap.feature.settings.presentation.SettingsScreen
import com.middleton.studiosnap.feature.settings.presentation.di.settingsModule
import com.middleton.studiosnap.feature.splash.presentation.SplashScreen
import com.middleton.studiosnap.feature.splash.presentation.di.splashModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
                addHomeScreen()
                addStylePickerScreen()
                addProcessingScreen()
                addResultsScreen()
                addCreditStoreScreen()
                addHistoryScreen()
                addSettingsScreen()
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

private fun NavGraphBuilder.addHomeScreen() {
    composable<Route.Home> { backStackEntry ->
        val viewModel: HomeViewModel = koinViewModel()

        // Observe style picker result reactively
        val selectedStyleId by backStackEntry.savedStateHandle
            .getStateFlow<String?>(HomeNavigationAction.STYLE_PICKER_RESULT_KEY, null)
            .collectAsState()

        LaunchedEffect(selectedStyleId) {
            selectedStyleId?.let { styleId ->
                viewModel.handleAction(HomeUiAction.OnStyleSelected(styleId))
                backStackEntry.savedStateHandle
                    .remove<String>(HomeNavigationAction.STYLE_PICKER_RESULT_KEY)
            }
        }

        HomeScreen(viewModel = viewModel)
    }
}

private fun NavGraphBuilder.addStylePickerScreen() {
    composable<Route.StylePicker> { backStackEntry ->
        val navController: androidx.navigation.NavHostController = koinInject()
        val route = backStackEntry.toRoute<Route.StylePicker>()

        StylePickerScreen(
            currentSelectedStyleId = route.currentStyleId,
            onStyleSelected = { styleId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(HomeNavigationAction.STYLE_PICKER_RESULT_KEY, styleId)
                navController.popBackStack()
            },
            onClose = {
                navController.popBackStack()
            }
        )
    }
}

private fun NavGraphBuilder.addProcessingScreen() {
    composable<Route.Processing> {
        ProcessingScreen()
    }
}

private fun NavGraphBuilder.addResultsScreen() {
    composable<Route.Results> {
        ResultsScreen()
    }
}

private fun NavGraphBuilder.addCreditStoreScreen() {
    composable<Route.CreditStore> {
        PaywallScreen()
    }
}

private fun NavGraphBuilder.addHistoryScreen() {
    composable<Route.History> {
        HistoryScreen()
    }
}

private fun NavGraphBuilder.addSettingsScreen() {
    composable<Route.Settings> {
        SettingsScreen()
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
