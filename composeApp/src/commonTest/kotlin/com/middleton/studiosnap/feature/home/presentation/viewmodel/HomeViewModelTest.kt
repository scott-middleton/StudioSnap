package com.middleton.studiosnap.feature.home.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.data.repository.GenerationConfigHolderImpl
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.core.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest : BaseViewModelTest() {

    private val testStyles = listOf(
        Style(
            id = "clean_white",
            nameKey = "Clean White",
            categories = setOf(StyleCategory.ALL),
            thumbnailResName = "style_clean_white",
            kontextPrompt = "White bg"
        ),
        Style(
            id = "warm_linen",
            nameKey = "Warm Linen",
            categories = setOf(StyleCategory.CLOTHING, StyleCategory.JEWELLERY),
            thumbnailResName = "style_warm_linen",
            kontextPrompt = "Linen bg"
        ),
        Style(
            id = "morning_kitchen",
            nameKey = "Morning Kitchen",
            categories = setOf(StyleCategory.FOOD, StyleCategory.HOMEWARE),
            thumbnailResName = "style_morning_kitchen",
            kontextPrompt = "Kitchen bg"
        )
    )

    private fun createViewModel(
        styles: List<Style> = testStyles,
        creditBalance: Int = 10,
        isSignedIn: Boolean = false,
        lastCategory: String = "ALL"
    ): HomeViewModel {
        return HomeViewModel(
            styleRepository = FakeStyleRepository(styles),
            creditQueries = FakeCreditQueries(creditBalance),
            authService = FakeAuthService(isSignedIn),
            userPreferencesRepository = FakeUserPreferencesRepository(lastCategory = lastCategory),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService()
        )
    }

    @Test
    fun `initial state loads all styles`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(3, state.styles.size)
        assertEquals(StyleCategory.ALL, state.selectedCategory)
    }

    @Test
    fun `selecting category filters styles`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnCategorySelected(StyleCategory.FOOD))
        val state = viewModel.uiState.value
        assertEquals(StyleCategory.FOOD, state.selectedCategory)
        assertEquals(1, state.styles.size)
        assertEquals("morning_kitchen", state.styles.first().id)
    }

    @Test
    fun `adding photos updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1", "uri2")))
        assertEquals(2, viewModel.uiState.value.photoCount)
        assertTrue(viewModel.uiState.value.hasPhotos)
    }

    @Test
    fun `removing photo updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        val photoId = viewModel.uiState.value.photos.first().id
        viewModel.handleAction(HomeUiAction.OnPhotoRemoved(photoId))
        assertEquals(0, viewModel.uiState.value.photoCount)
        assertFalse(viewModel.uiState.value.hasPhotos)
    }

    @Test
    fun `cannot add more than MAX_PHOTOS`() {
        val viewModel = createViewModel()
        val uris = (1..15).map { "uri_$it" }
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(uris))
        assertEquals(10, viewModel.uiState.value.photoCount)
    }

    @Test
    fun `selecting style updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStyleSelected("warm_linen"))
        assertNotNull(viewModel.uiState.value.selectedStyle)
        assertEquals("warm_linen", viewModel.uiState.value.selectedStyle!!.id)
    }

    @Test
    fun `canGenerate is false without photos`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        assertFalse(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `canGenerate is false without style`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        assertFalse(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `canGenerate is true with photos and style`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `generate navigates to processing when valid`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `generate does nothing without photos`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `shadow toggle updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnShadowToggled(true))
        assertTrue(viewModel.uiState.value.shadow)
    }

    @Test
    fun `reflection toggle updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnReflectionToggled(true))
        assertTrue(viewModel.uiState.value.reflection)
    }

    @Test
    fun `export format updates state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnExportFormatSelected(ExportFormat.ETSY_SQUARE))
        assertEquals(ExportFormat.ETSY_SQUARE, viewModel.uiState.value.exportFormat)
    }

    @Test
    fun `settings click navigates`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnSettingsClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToSettings)
    }

    @Test
    fun `history click navigates`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnHistoryClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToHistory)
    }

    @Test
    fun `credit balance click navigates to store`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnCreditBalanceClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToCreditStore)
    }

    @Test
    fun `error dismissed clears error`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnErrorDismissed)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `credit balance loaded from credit queries`() {
        val viewModel = createViewModel(creditBalance = 42)
        assertEquals(42, viewModel.uiState.value.creditBalance)
    }

    // --- Fakes ---

    private class FakeStyleRepository(private val styles: List<Style>) : StyleRepository {
        override fun getAllStyles(): List<Style> = styles
        override fun getStylesByCategory(category: StyleCategory): List<Style> {
            if (category == StyleCategory.ALL) return styles
            return styles.filter { category in it.categories }
        }
        override fun getStyleById(id: String): Style? = styles.find { it.id == id }
    }

    private class FakeCreditQueries(private val balance: Int) : CreditQueries {
        override suspend fun getUserCredits() = Result.success(UserCredits(balance))
        override suspend fun refreshCredits() = Result.success(UserCredits(balance))
        override fun observeCredits(): Flow<UserCredits> = flowOf(UserCredits(balance))
    }

    private class FakeAuthService(private val signedIn: Boolean) : AuthService {
        override val isSignedIn: StateFlow<Boolean> = MutableStateFlow(signedIn)
        override suspend fun awaitInitialized(): Boolean = signedIn
        override suspend fun signIn(): Result<AuthUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun getCurrentUser(): AuthUser? = null
    }

    private class FakeUserPreferencesRepository(
        private val onboardingCompleted: Boolean = true,
        private val lastCategory: String = "ALL"
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
        override suspend fun getLastUsedCategoryFilter() = lastCategory
        override suspend fun setLastUsedCategoryFilter(category: String) {}
        override fun observePreferences(): Flow<UserPreferencesSnapshot> = flowOf(
            UserPreferencesSnapshot(
                hasCompletedOnboarding = onboardingCompleted,
                hasPurchasedCredits = false,
                freeDownloadsUsed = 0,
                totalPaidDownloads = 0,
                preferredQuality = "HIGH",
                lastUsedCategoryFilter = lastCategory
            )
        )
    }

    private class FakeAnalyticsService : AnalyticsService {
        override fun logEvent(name: String, params: Map<String, Any>) {}
    }
}
