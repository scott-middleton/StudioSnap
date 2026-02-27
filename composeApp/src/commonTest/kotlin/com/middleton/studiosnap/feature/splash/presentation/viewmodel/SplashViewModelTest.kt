package com.middleton.studiosnap.feature.splash.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.splash.presentation.navigation.SplashNavigationAction
import com.middleton.studiosnap.purchases.PurchasesIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertTrue

class SplashViewModelTest : BaseViewModelTest() {

    @Test
    fun `navigates to onboarding when onboarding not completed`() {
        val viewModel = createViewModel(hasCompletedOnboarding = false)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is SplashNavigationAction.GoToOnboarding)
    }

    @Test
    fun `navigates to home when onboarding completed`() {
        val viewModel = createViewModel(hasCompletedOnboarding = true)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is SplashNavigationAction.GoToHome)
    }

    private fun createViewModel(
        hasCompletedOnboarding: Boolean = false,
        isSignedIn: Boolean = false
    ): SplashViewModel {
        return SplashViewModel(
            authService = FakeAuthService(isSignedIn),
            creditManager = FakeCreditManager(),
            userPreferencesRepository = FakeUserPreferencesRepository(hasCompletedOnboarding),
            purchasesIdentifier = FakePurchasesIdentifier(),
            analyticsService = FakeAnalyticsService()
        )
    }

    // --- Fakes ---

    private class FakeAuthService(private val signedIn: Boolean) : AuthService {
        override val isSignedIn: StateFlow<Boolean> = MutableStateFlow(signedIn)
        override suspend fun awaitInitialized(): Boolean = signedIn
        override suspend fun signIn(): Result<AuthUser> = Result.failure(Exception())
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun getCurrentUser(): AuthUser? =
            if (signedIn) AuthUser(id = "user1", email = "test@test.com") else null
    }

    private class FakeCreditManager : CreditManager {
        override val credits: StateFlow<UserCredits?> = MutableStateFlow(UserCredits(0))
        override val isLoading: StateFlow<Boolean> = MutableStateFlow(false)
        override suspend fun loadCredits() = Result.success(UserCredits(0))
        override suspend fun refreshCredits() = Result.success(UserCredits(0))
        override fun clearCredits() {}
    }

    private class FakeUserPreferencesRepository(
        private val onboardingCompleted: Boolean
    ) : UserPreferencesRepository {
        override suspend fun hasCompletedOnboarding() = onboardingCompleted
        override suspend fun setHasCompletedOnboarding() {}
        override suspend fun hasPurchasedCredits() = false
        override suspend fun setHasPurchasedCredits() {}
        override suspend fun getFreeDownloadsUsed() = 0
        override suspend fun incrementFreeDownloads() {}
        override suspend fun incrementAndGetPaidDownloads() = 0
        override suspend fun getPreferredQuality() = "HIGH"
        override suspend fun setPreferredQuality(quality: String) {}
        override suspend fun getLastUsedCategoryFilter() = "ALL"
        override suspend fun setLastUsedCategoryFilter(category: String) {}
        override fun observePreferences(): Flow<UserPreferencesSnapshot> = flowOf(
            UserPreferencesSnapshot(onboardingCompleted, false, 0, 0, "HIGH", "ALL")
        )
    }

    private class FakePurchasesIdentifier : PurchasesIdentifier {
        override suspend fun identifyUser(userId: String) {}
    }

    private class FakeAnalyticsService : AnalyticsService {
        override fun logEvent(name: String, params: Map<String, Any>) {}
    }
}
