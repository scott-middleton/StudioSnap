package com.middleton.studiosnap.feature.onboarding.presentation.viewmodel

import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.navigation.OnboardingNavigationAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingViewModelTest : BaseViewModelTest() {

    private fun createViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(),
            analyticsService = FakeAnalyticsService()
        )
    }

    @Test
    fun `initial state is page 0`() {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `next page increments current page`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `next page does not exceed total pages`() {
        val viewModel = createViewModel()
        repeat(10) { viewModel.handleAction(OnboardingUiAction.NextPage) }
        assertEquals(OnboardingViewModel.TOTAL_PAGES - 1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to specific page updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(2))
        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to invalid page is ignored`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(-1))
        assertEquals(0, viewModel.uiState.value.currentPage)
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(100))
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `get started navigates to home`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(viewModel.navigationEvent.value is OnboardingNavigationAction.GoToHome)
    }

    @Test
    fun `navigation handled clears event`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        viewModel.onNavigationHandled()
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `get started marks onboarding complete`() {
        val fakePrefs = FakeUserPreferencesRepository()
        val viewModel = OnboardingViewModel(
            userPreferencesRepository = fakePrefs,
            analyticsService = FakeAnalyticsService()
        )
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(fakePrefs.onboardingCompleted)
    }

    // --- Fakes ---

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        var onboardingCompleted = false

        override suspend fun hasCompletedOnboarding() = onboardingCompleted
        override suspend fun setHasCompletedOnboarding() { onboardingCompleted = true }
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
            UserPreferencesSnapshot(false, false, 0, 0, "HIGH", "ALL")
        )
    }

    private class FakeAnalyticsService : AnalyticsService {
        override fun logEvent(name: String, params: Map<String, Any>) {}
    }
}
