package com.middleton.studiosnap.feature.splash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.FreeGenerationGate
import com.middleton.studiosnap.feature.splash.presentation.navigation.SplashNavigationAction
import com.middleton.studiosnap.purchases.PurchasesIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authService: AuthService,
    private val creditManager: CreditManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val purchasesIdentifier: PurchasesIdentifier,
    private val analyticsService: AnalyticsService,
    private val freeGenerationGate: FreeGenerationGate
) : ViewModel() {

    private val _navigationEvent = MutableStateFlow<SplashNavigationAction?>(null)
    val navigationEvent: StateFlow<SplashNavigationAction?> = _navigationEvent.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            val minimumDelayJob = async { delay(MIN_SPLASH_DURATION_MS) }
            initializeUserSession()
            syncFreeTrialState()
            val hasCompletedOnboarding = userPreferencesRepository.hasCompletedOnboarding()
            minimumDelayJob.await()

            if (!hasCompletedOnboarding) {
                analyticsService.logEvent(AnalyticsEvents.APP_FIRST_OPEN)
            }

            _navigationEvent.value = if (!hasCompletedOnboarding) {
                SplashNavigationAction.GoToOnboarding
            } else {
                SplashNavigationAction.GoToHome
            }
        }
    }

    private suspend fun initializeUserSession(): Boolean {
        val isSignedIn = authService.awaitInitialized()
        if (isSignedIn) {
            val user = authService.getCurrentUser()
            if (user != null) {
                purchasesIdentifier.identifyUser(user.id)
                creditManager.loadCredits()
            }
        }
        return isSignedIn
    }

    // Syncs the server-side free trial state into local SQLite.
    // If local SQLite was cleared (reinstall), this restores the flag so the UI is correct.
    // Also marks free trial as used if the user has credits — they're past the free trial.
    private suspend fun syncFreeTrialState() {
        try {
            if (userPreferencesRepository.hasUsedFreeGeneration()) return

            // A signed-in user with credits has already moved past the free trial.
            // Credits are loaded by initializeUserSession() before this is called.
            val hasCredits = creditManager.credits.value?.amount?.let { it > 0 } == true
            if (hasCredits) {
                userPreferencesRepository.setHasUsedFreeGeneration()
                return
            }

            val usedOnServer = freeGenerationGate.checkFreeGenerationUsed()
            if (usedOnServer) {
                userPreferencesRepository.setHasUsedFreeGeneration()
            }
        } catch (_: Exception) {
            // Non-critical — worst case user sees free trial UI but server will gate the actual claim
        }
    }

    companion object {
        private const val MIN_SPLASH_DURATION_MS = 2500L
    }
}
