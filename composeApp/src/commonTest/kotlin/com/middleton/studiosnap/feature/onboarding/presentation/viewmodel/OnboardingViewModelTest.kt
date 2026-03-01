package com.middleton.studiosnap.feature.onboarding.presentation.viewmodel

import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
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

    private fun createViewModel(
        prefsRepository: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService()
    ): OnboardingViewModel {
        return OnboardingViewModel(
            userPreferencesRepository = prefsRepository,
            analyticsService = analyticsService
        )
    }

    // --- Page count ---

    @Test
    fun `total pages is 4`() {
        assertEquals(4, OnboardingViewModel.TOTAL_PAGES)
    }

    // --- Initial state ---

    @Test
    fun `initial state is page 0`() {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `initial navigation event is null`() {
        val viewModel = createViewModel()
        assertNull(viewModel.navigationEvent.value)
    }

    // --- NextPage ---

    @Test
    fun `next page increments current page`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `next page progresses through all 4 pages`() {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.uiState.value.currentPage)

        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(1, viewModel.uiState.value.currentPage)

        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(2, viewModel.uiState.value.currentPage)

        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(3, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `next page does not exceed last page`() {
        val viewModel = createViewModel()
        repeat(10) { viewModel.handleAction(OnboardingUiAction.NextPage) }
        assertEquals(OnboardingViewModel.TOTAL_PAGES - 1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `next page on last page stays on last page`() {
        val viewModel = createViewModel()
        // Navigate to last page
        repeat(OnboardingViewModel.TOTAL_PAGES - 1) {
            viewModel.handleAction(OnboardingUiAction.NextPage)
        }
        assertEquals(3, viewModel.uiState.value.currentPage)

        // Try to go further
        viewModel.handleAction(OnboardingUiAction.NextPage)
        assertEquals(3, viewModel.uiState.value.currentPage)
    }

    // --- NavigateToPage ---

    @Test
    fun `navigate to specific page updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(2))
        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to last page succeeds`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(3))
        assertEquals(3, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to first page from middle succeeds`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(2))
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(0))
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to negative page is ignored`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(-1))
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to page beyond total is ignored`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(4))
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `navigate to page exactly at total is ignored`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(OnboardingViewModel.TOTAL_PAGES))
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    // --- GetStarted ---

    @Test
    fun `get started navigates to home`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(viewModel.navigationEvent.value is OnboardingNavigationAction.GoToHome)
    }

    @Test
    fun `get started marks onboarding complete`() {
        val fakePrefs = FakeUserPreferencesRepository()
        val viewModel = createViewModel(prefsRepository = fakePrefs)
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(fakePrefs.onboardingCompleted)
    }

    @Test
    fun `get started logs onboarding completed analytics event`() {
        val fakeAnalytics = FakeAnalyticsService()
        val viewModel = createViewModel(analyticsService = fakeAnalytics)
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(fakeAnalytics.loggedEvents.contains(AnalyticsEvents.ONBOARDING_COMPLETED))
    }

    @Test
    fun `get started from any page triggers navigation`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.NavigateToPage(1))
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        assertTrue(viewModel.navigationEvent.value is OnboardingNavigationAction.GoToHome)
    }

    // --- OnNavigationHandled ---

    @Test
    fun `navigation handled clears event`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.GetStarted)
        viewModel.handleAction(OnboardingUiAction.OnNavigationHandled)
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `navigation handled when no event is no-op`() {
        val viewModel = createViewModel()
        viewModel.handleAction(OnboardingUiAction.OnNavigationHandled)
        assertNull(viewModel.navigationEvent.value)
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
        val loggedEvents = mutableListOf<String>()

        override fun logEvent(name: String, params: Map<String, Any>) {
            loggedEvents.add(name)
        }
    }
}
