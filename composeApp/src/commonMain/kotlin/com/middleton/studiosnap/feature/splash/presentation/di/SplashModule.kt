package com.middleton.studiosnap.feature.splash.presentation.di

import com.middleton.studiosnap.feature.splash.presentation.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashModule = module {
    viewModel {
        SplashViewModel(
            authService = get(),
            ensureWelcomeCreditsUseCase = get(),
            userPreferencesRepository = get(),
            purchasesIdentifier = get(),
            analyticsService = get()
        )
    }
}