package com.middleton.studiosnap.feature.home.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.usecase.ObserveCreditStateUseCase
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.data.repository.GenerationConfigHolderImpl
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
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
            displayName = UiText.DynamicString("Clean White"),
            categories = setOf(StyleCategory.ALL),
            thumbnail = null,
            kontextPrompt = "White bg"
        ),
        Style(
            id = "warm_linen",
            displayName = UiText.DynamicString("Warm Linen"),
            categories = setOf(StyleCategory.CLOTHING, StyleCategory.JEWELLERY),
            thumbnail = null,
            kontextPrompt = "Linen bg"
        ),
        Style(
            id = "morning_kitchen",
            displayName = UiText.DynamicString("Morning Kitchen"),
            categories = setOf(StyleCategory.FOOD, StyleCategory.HOMEWARE),
            thumbnail = null,
            kontextPrompt = "Kitchen bg"
        )
    )

    private fun createViewModel(
        styles: List<Style> = testStyles,
        creditBalance: Int = 10,
        isSignedIn: Boolean = false,
        historyItems: List<GenerationResult.Success> = emptyList(),
        configHolder: GenerationConfigHolder = GenerationConfigHolderImpl()
    ): HomeViewModel {
        val authService = FakeAuthService(isSignedIn)
        val creditManager = FakeCreditManager(creditBalance)
        return HomeViewModel(
            styleRepository = FakeStyleRepository(styles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = configHolder,
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(historyItems)
        )
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
    fun `canGenerate is true with photos and style when signed in with credits`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `generate navigates to processing when valid`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `generate sets a non-empty batchId on the config`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val config = holder.currentConfig
        assertNotNull(config)
        assertTrue(config!!.batchId.isNotEmpty())
    }

    @Test
    fun `generate shows sign in sheet when not signed in`() {
        val viewModel = createViewModel(isSignedIn = false)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertTrue(viewModel.uiState.value.showSignIn)
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `generate navigates to credit store when not enough credits`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 0)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStyleSelected("clean_white"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToCreditStore)
    }

    @Test
    fun `generate does nothing without photos when signed in`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
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
    fun `credit balance loaded from credit manager when signed in`() {
        val viewModel = createViewModel(creditBalance = 42, isSignedIn = true)
        assertEquals(42, viewModel.uiState.value.creditBalance)
    }

    @Test
    fun `credit balance is zero when signed out`() {
        val viewModel = createViewModel(creditBalance = 42, isSignedIn = false)
        assertEquals(0, viewModel.uiState.value.creditBalance)
        assertFalse(viewModel.uiState.value.isSignedIn)
    }

    @Test
    fun `style picker click navigates with null style id when none selected`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylePickerClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToStylePicker)
        assertNull((nav as HomeNavigationAction.GoToStylePicker).currentStyleId)
    }

    @Test
    fun `style picker click navigates with current style id when style selected`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStyleSelected("warm_linen"))
        viewModel.handleAction(HomeUiAction.OnNavigationHandled)
        viewModel.handleAction(HomeUiAction.OnStylePickerClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToStylePicker)
        assertEquals("warm_linen", (nav as HomeNavigationAction.GoToStylePicker).currentStyleId)
    }

    @Test
    fun `recent generations loaded from history repository`() {
        val result = GenerationResult.Success(
            generationId = "gen_1",
            inputPhoto = ProductPhoto(id = "p1", localUri = "content://p1"),
            previewUri = "/cache/preview.jpg",
            style = testStyles.first(),
            createdAt = 1000L
        )
        val viewModel = createViewModel(historyItems = listOf(result))
        assertEquals(1, viewModel.uiState.value.recentGenerations.size)
        assertEquals("gen_1", viewModel.uiState.value.recentGenerations.first().id)
    }

    @Test
    fun `recent generations empty when no history`() {
        val viewModel = createViewModel(historyItems = emptyList())
        assertTrue(viewModel.uiState.value.recentGenerations.isEmpty())
    }

    @Test
    fun `recent generations capped at 5 when history has more`() {
        val items = (1..8).map { i ->
            GenerationResult.Success(
                generationId = "gen_$i",
                inputPhoto = ProductPhoto(id = "p$i", localUri = "content://p$i"),
                previewUri = "/cache/preview_$i.jpg",
                style = testStyles.first(),
                createdAt = 1000L * i
            )
        }
        val viewModel = createViewModel(historyItems = items)
        assertEquals(5, viewModel.uiState.value.recentGenerations.size)
    }

    @Test
    fun `view all history click navigates to history`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnViewAllHistoryClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToHistory)
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

    private class FakeAuthService(private val signedIn: Boolean) : AuthService {
        override val isSignedIn: StateFlow<Boolean> = MutableStateFlow(signedIn)
        override suspend fun awaitInitialized(): Boolean = signedIn
        override suspend fun signIn(): Result<AuthUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun getCurrentUser(): AuthUser? = null
    }

    private class FakeCreditManager(private val balance: Int) : CreditManager {
        private val _credits = MutableStateFlow<UserCredits?>(UserCredits(balance))
        override val credits: StateFlow<UserCredits?> = _credits
        private val _isLoading = MutableStateFlow(false)
        override val isLoading: StateFlow<Boolean> = _isLoading
        override suspend fun loadCredits(): Result<UserCredits> = Result.success(UserCredits(balance))
        override suspend fun refreshCredits(): Result<UserCredits> = Result.success(UserCredits(balance))
        override fun clearCredits() { _credits.value = null }
    }

    private class FakeAnalyticsService : AnalyticsService {
        override fun logEvent(name: String, params: Map<String, Any>) {}
    }

    private class FakeHistoryRepository(
        private val items: List<GenerationResult.Success> = emptyList()
    ) : HistoryRepository {
        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(items)
        override fun getSessions() = flowOf(emptyList<HistorySession>())
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String) = items.find { it.generationId == id }
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }
}
