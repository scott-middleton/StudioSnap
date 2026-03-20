package com.middleton.studiosnap

import com.middleton.studiosnap.core.data.di.coreDataModule
import com.middleton.studiosnap.core.data.di.firebaseAuthModule
import com.middleton.studiosnap.core.data.di.platformModule
import com.middleton.studiosnap.core.presentation.di.coreNavigationModule
import com.middleton.studiosnap.feature.auth.presentation.di.authModule
import com.middleton.studiosnap.feature.history.presentation.di.historyModule
import com.middleton.studiosnap.feature.home.presentation.di.homeModule
import com.middleton.studiosnap.feature.onboarding.presentation.di.onboardingModule
import com.middleton.studiosnap.feature.paywall.presentation.di.paywallModule
import com.middleton.studiosnap.feature.processing.presentation.di.processingModule
import com.middleton.studiosnap.feature.results.presentation.di.resultsModule
import com.middleton.studiosnap.feature.settings.presentation.di.settingsModule
import com.middleton.studiosnap.feature.splash.presentation.di.splashModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * Initialises Koin with all app modules.
 *
 * Safe to call multiple times — skips if Koin is already running.
 * Call this from the Android Application class and the iOS entry point
 * *before* any Compose content is rendered.
 */
fun initKoin() {
    if (GlobalContext.getOrNull() != null) return
    startKoin {
        modules(
            platformModule(),
            firebaseAuthModule,
            coreDataModule,
            coreNavigationModule,
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
    }
}
