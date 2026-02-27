package com.middleton.studiosnap.feature.settings.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest : BaseViewModelTest() {

    @Test
    fun `init loads preferred quality`() {
        val vm = createViewModel(preferredQuality = "STANDARD")
        assertEquals("STANDARD", vm.uiState.value.preferredQuality)
    }

    @Test
    fun `init loads signed in state`() {
        val vm = createViewModel(isSignedIn = true)
        assertTrue(vm.uiState.value.isSignedIn)
    }

    @Test
    fun `observes credit balance`() {
        val creditsFlow = MutableStateFlow(UserCredits(15))
        val vm = createViewModel(creditsFlow = creditsFlow)
        assertEquals(15, vm.uiState.value.creditBalance)

        creditsFlow.value = UserCredits(8)
        assertEquals(8, vm.uiState.value.creditBalance)
    }

    @Test
    fun `quality change updates state and persists`() {
        val prefsRepo = FakeUserPreferencesRepository()
        val vm = createViewModel(prefsRepo = prefsRepo)

        vm.handleAction(SettingsUiAction.OnQualityChanged("STANDARD"))

        assertEquals("STANDARD", vm.uiState.value.preferredQuality)
        assertEquals("STANDARD", prefsRepo.quality)
    }

    @Test
    fun `buy credits navigates to credit store`() {
        val vm = createViewModel()
        vm.handleAction(SettingsUiAction.OnBuyCreditsClicked)
        assertIs<SettingsNavigationAction.GoToCreditStore>(vm.navigationEvent.value)
    }

    @Test
    fun `back navigates back`() {
        val vm = createViewModel()
        vm.handleAction(SettingsUiAction.OnBackClicked)
        assertIs<SettingsNavigationAction.GoBack>(vm.navigationEvent.value)
    }

    @Test
    fun `navigation handled clears event`() {
        val vm = createViewModel()
        vm.handleAction(SettingsUiAction.OnBackClicked)
        assertIs<SettingsNavigationAction.GoBack>(vm.navigationEvent.value)

        vm.onNavigationHandled()
        assertNull(vm.navigationEvent.value)
    }

    // --- Factory ---

    private fun createViewModel(
        preferredQuality: String = "HIGH",
        isSignedIn: Boolean = false,
        creditsFlow: Flow<UserCredits> = flowOf(UserCredits(10)),
        prefsRepo: FakeUserPreferencesRepository = FakeUserPreferencesRepository(preferredQuality)
    ): SettingsViewModel {
        return SettingsViewModel(
            creditQueries = FakeCreditQueries(creditsFlow),
            authService = FakeAuthService(isSignedIn),
            userPreferencesRepository = prefsRepo
        )
    }

    // --- Fakes ---

    private class FakeCreditQueries(
        private val flow: Flow<UserCredits> = flowOf(UserCredits(10))
    ) : CreditQueries {
        override suspend fun getUserCredits() = Result.success(UserCredits(10))
        override suspend fun refreshCredits() = Result.success(UserCredits(10))
        override fun observeCredits() = flow
    }

    private class FakeAuthService(signedIn: Boolean = false) : AuthService {
        override val isSignedIn: StateFlow<Boolean> = MutableStateFlow(signedIn)
        override suspend fun awaitInitialized() = isSignedIn.value
        override suspend fun signIn() = Result.success(AuthUser(id = "user_1", email = null, displayName = null, provider = com.middleton.studiosnap.core.domain.model.AuthProvider.GOOGLE))
        override suspend fun signOut() = Result.success(Unit)
        override suspend fun getCurrentUser() = if (isSignedIn.value) AuthUser(id = "user_1", email = null, displayName = null, provider = com.middleton.studiosnap.core.domain.model.AuthProvider.GOOGLE) else null
    }

    private class FakeUserPreferencesRepository(
        var quality: String = "HIGH"
    ) : UserPreferencesRepository {
        override suspend fun hasCompletedOnboarding() = true
        override suspend fun setHasCompletedOnboarding() {}
        override suspend fun hasPurchasedCredits() = false
        override suspend fun setHasPurchasedCredits() {}
        override suspend fun getFreeDownloadsUsed() = 0
        override suspend fun incrementFreeDownloads() {}
        override suspend fun incrementAndGetPaidDownloads() = 1
        override suspend fun getPreferredQuality() = quality
        override suspend fun setPreferredQuality(quality: String) { this.quality = quality }
        override suspend fun getLastUsedCategoryFilter() = "ALL"
        override suspend fun setLastUsedCategoryFilter(category: String) {}
        override fun observePreferences() = flowOf(
            UserPreferencesSnapshot(
                hasCompletedOnboarding = true,
                hasPurchasedCredits = false,
                freeDownloadsUsed = 0,
                totalPaidDownloads = 0,
                preferredQuality = quality,
                lastUsedCategoryFilter = "ALL"
            )
        )
    }
}
