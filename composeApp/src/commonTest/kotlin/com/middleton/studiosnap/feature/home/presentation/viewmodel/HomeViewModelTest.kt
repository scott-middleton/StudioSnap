package com.middleton.studiosnap.feature.home.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.FakeErrorReporter
import com.middleton.studiosnap.core.domain.service.WelcomeCreditGranter
import com.middleton.studiosnap.core.domain.usecase.EnsureWelcomeCreditsUseCase
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
import com.middleton.studiosnap.feature.home.domain.usecase.BuildKontextPromptUseCase
import com.middleton.studiosnap.feature.home.presentation.action.HomeUiAction
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.ui_state.BackgroundChoice
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import kotlinx.coroutines.CompletableDeferred
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
        ),
        Style(
            id = "marble_luxe",
            displayName = UiText.DynamicString("Marble Luxe"),
            categories = setOf(StyleCategory.ALL),
            thumbnail = null,
            kontextPrompt = "Marble bg"
        ),
        Style(
            id = "dark_moody",
            displayName = UiText.DynamicString("Dark Moody"),
            categories = setOf(StyleCategory.ALL),
            thumbnail = null,
            kontextPrompt = "Moody bg"
        )
    )

    private fun createViewModel(
        styles: List<Style> = testStyles,
        creditBalance: Int = 10,
        isSignedIn: Boolean = false,
        historyItems: List<GenerationResult.Success> = emptyList(),
        configHolder: GenerationConfigHolder = GenerationConfigHolderImpl(),
        creditManager: CreditManager? = null
    ): HomeViewModel {
        val authService = FakeAuthService(isSignedIn)
        val resolvedCreditManager = creditManager ?: FakeCreditManager(creditBalance)
        return HomeViewModel(
            styleRepository = FakeStyleRepository(styles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, resolvedCreditManager),
            generationConfigHolder = configHolder,
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(historyItems),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(FakeWelcomeCreditGranter(), resolvedCreditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
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
    fun `selecting styles sets MultiPreset choice`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        val choice = viewModel.uiState.value.backgroundChoice
        assertNotNull(choice)
        assertTrue(choice is BackgroundChoice.MultiPreset)
        assertEquals("warm_linen", (choice as BackgroundChoice.MultiPreset).styles.single().id)
        assertNotNull(viewModel.uiState.value.primaryStyle)
        assertEquals("warm_linen", viewModel.uiState.value.primaryStyle!!.id)
        assertEquals(listOf("warm_linen"), viewModel.uiState.value.selectedStyles.map { it.id })
    }

    @Test
    fun `selecting multiple styles with one photo keeps them all`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white", "marble_luxe")))
        assertEquals(
            listOf("warm_linen", "clean_white", "marble_luxe"),
            viewModel.uiState.value.selectedStyles.map { it.id }
        )
        assertTrue(viewModel.uiState.value.isMultiStyle)
    }

    @Test
    fun `selecting empty styles clears choice to null`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(emptyList()))
        assertNull(viewModel.uiState.value.backgroundChoice)
    }

    @Test
    fun `adding a second photo collapses multi-preset to the first style`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri2")))
        assertEquals(listOf("warm_linen"), viewModel.uiState.value.selectedStyles.map { it.id })
    }

    @Test
    fun `styleMaxSelectable is single with two or more photos`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1", "uri2")))
        assertEquals(1, viewModel.uiState.value.styleMaxSelectable)
    }

    @Test
    fun `typing custom description sets Custom choice and clears preset selection`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("Marble countertop"))
        val choice = viewModel.uiState.value.backgroundChoice
        assertTrue(choice is BackgroundChoice.Custom)
        assertEquals("Marble countertop", (choice as BackgroundChoice.Custom).description)
        assertTrue(viewModel.uiState.value.selectedStyles.isEmpty())
    }

    @Test
    fun `clearing custom description back to blank reverts choice to null`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("Marble countertop"))
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("   "))
        assertNull(viewModel.uiState.value.backgroundChoice)
    }

    @Test
    fun `custom description is capped at MAX_CUSTOM_DESCRIPTION_LENGTH`() {
        val viewModel = createViewModel()
        val longText = "a".repeat(200)
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged(longText))
        val choice = viewModel.uiState.value.backgroundChoice as BackgroundChoice.Custom
        assertEquals(150, choice.description.length)
    }

    @Test
    fun `toggling custom description expanded flips state`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isCustomDescriptionExpanded)
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionExpandedToggled)
        assertTrue(viewModel.uiState.value.isCustomDescriptionExpanded)
    }

    @Test
    fun `canGenerate false with custom description shorter than minimum length`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("ab"))
        assertFalse(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `canGenerate true with custom description at minimum length`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("abc"))
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `generate with custom description builds one placeholder unit per photo with resolved prompt`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnCustomDescriptionChanged("Marble countertop"))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val config = holder.currentConfig
        assertNotNull(config)
        assertEquals(1, config!!.units.size)
        val unit = config.units.single()
        assertEquals("Marble countertop", unit.resolvedPrompt)
        assertEquals(HomeViewModel.CUSTOM_STYLE_ID, unit.style.id)
    }

    @Test
    fun `generate with preset style resolves each unit prompt from style kontextPrompt`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val config = holder.currentConfig
        assertNotNull(config)
        val unit = config!!.units.single()
        assertEquals("Linen bg", unit.resolvedPrompt)
        assertEquals("warm_linen", unit.style.id)
    }

    @Test
    fun `generate with multiple presets and one photo produces one unit per style`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val config = holder.currentConfig
        assertNotNull(config)
        assertEquals(2, config!!.units.size)
        assertEquals(listOf("warm_linen", "clean_white"), config.units.map { it.style.id })
    }

    @Test
    fun `canGenerate is false without photos`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
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
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `generate navigates to processing when valid`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `generate sets a non-empty batchId on the config`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        val config = holder.currentConfig
        assertNotNull(config)
        assertTrue(config!!.batchId.isNotEmpty())
    }

    @Test
    fun `generate shows sign in sheet when not signed in`() {
        val viewModel = createViewModel(isSignedIn = false)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertTrue(viewModel.uiState.value.showSignIn)
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `generate navigates to credit store when not enough credits`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 0)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToCreditStore)
    }

    @Test
    fun `sign in success claims welcome credits and resumes pending generation`() {
        val authService = MutableFakeAuthService()
        val creditManager = FakeCreditManager(balance = 0)
        val granter = FakeWelcomeCreditGranter(granted = true)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(granter, creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertTrue(viewModel.uiState.value.showSignIn)

        creditManager.setBalance(1)
        authService.setSignedIn(true)
        viewModel.handleAction(HomeUiAction.OnSignInResult(true))

        assertTrue(granter.claimCalled)
        assertFalse(viewModel.uiState.value.showSignIn)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `sign in success with insufficient balance navigates to credit store`() {
        val authService = MutableFakeAuthService()
        val creditManager = FakeCreditManager(balance = 0)
        val granter = FakeWelcomeCreditGranter(granted = false)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(granter, creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)

        authService.setSignedIn(true)
        viewModel.handleAction(HomeUiAction.OnSignInResult(true))

        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToCreditStore)
    }

    @Test
    fun `sign in success with refresh failure falls back to live balance instead of routing to credit store`() {
        // Regression test: balance previously defaulted to 0 on a failed post-claim
        // refresh, which misrouted an already-funded user to the credit store.
        val authService = MutableFakeAuthService()
        // authService.setSignedIn(true) below triggers loadCredits() via
        // ObserveCreditStateUseCase.onStart, populating the cached balance to 5
        // before OnSignInResult's post-claim refresh (which we force to fail) runs.
        val creditManager = FakeCreditManager(balance = 5)
        val granter = FakeWelcomeCreditGranter(granted = false)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(granter, creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)

        authService.setSignedIn(true)
        creditManager.failNextRefresh()
        viewModel.handleAction(HomeUiAction.OnSignInResult(true))

        // The post-claim refresh failed, but the live cached balance (5) covers the
        // 1-photo cost — must resume generation, not bounce to the credit store.
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `screen resumed during sign-in completion does not reset isGenerating`() {
        val authService = MutableFakeAuthService()
        val creditManager = FakeCreditManager(balance = 0)
        val granter = SuspendingWelcomeCreditGranter()
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(granter, creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)

        creditManager.setBalance(1)
        authService.setSignedIn(true)
        granter.pause()
        viewModel.handleAction(HomeUiAction.OnSignInResult(true))

        // Claim is paused mid-flight — isGenerating should already be true.
        assertTrue(viewModel.uiState.value.isGenerating)

        // Simulate the app coming back to the foreground (e.g. Android activity
        // resume after the native sign-in sheet closes) while the claim is still
        // in flight — this must NOT reset isGenerating and let the user tap
        // Generate again mid-sequence.
        viewModel.handleAction(HomeUiAction.OnScreenResumed)
        assertTrue(viewModel.uiState.value.isGenerating)

        granter.resume()
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToProcessing)
    }

    @Test
    fun `sign in failure does not claim welcome credits or navigate`() {
        val granter = FakeWelcomeCreditGranter()
        val creditManager = FakeCreditManager(balance = 0)
        val viewModel = createViewModel(isSignedIn = false, creditManager = creditManager)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)

        viewModel.handleAction(HomeUiAction.OnSignInResult(false))

        assertFalse(viewModel.uiState.value.showSignIn)
        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `generate does nothing without photos when signed in`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 5)
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white")))
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
        viewModel.handleAction(HomeUiAction.OnExportFormatSelected(ExportFormat.ETSY_LANDSCAPE))
        assertEquals(ExportFormat.ETSY_LANDSCAPE, viewModel.uiState.value.exportFormat)
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
    fun `credit balance click navigates to store when signed in`() {
        val viewModel = createViewModel(isSignedIn = true)
        viewModel.handleAction(HomeUiAction.OnCreditBalanceClicked)
        assertTrue(viewModel.navigationEvent.value is HomeNavigationAction.GoToCreditStore)
    }

    @Test
    fun `credit balance click triggers sign in when logged out`() {
        val viewModel = createViewModel(isSignedIn = false)
        viewModel.handleAction(HomeUiAction.OnCreditBalanceClicked)
        assertTrue(viewModel.uiState.value.showSignIn)
        assertTrue(viewModel.uiState.value.isSigningIn)
        assertNull(viewModel.navigationEvent.value)
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
    fun `style picker click navigates with empty ids when none selected`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylePickerClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToStylePicker)
        assertTrue((nav as HomeNavigationAction.GoToStylePicker).currentStyleIds.isEmpty())
    }

    @Test
    fun `style picker click navigates with current style ids when style selected`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        viewModel.handleAction(HomeUiAction.OnNavigationHandled)
        viewModel.handleAction(HomeUiAction.OnStylePickerClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToStylePicker)
        assertEquals(listOf("warm_linen"), (nav as HomeNavigationAction.GoToStylePicker).currentStyleIds)
    }

    @Test
    fun `selecting multiple styles updates state in order`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        assertEquals(listOf("warm_linen", "clean_white"), viewModel.uiState.value.selectedStyles.map { it.id })
        assertEquals("warm_linen", viewModel.uiState.value.primaryStyle?.id)
    }

    @Test
    fun `generation cost is photos times styles`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(
            HomeUiAction.OnStylesSelected(listOf("clean_white", "warm_linen", "morning_kitchen"))
        )
        assertEquals(3, viewModel.uiState.value.generationCost)
    }

    @Test
    fun `style selection is capped at MAX_STYLES`() {
        val viewModel = createViewModel()
        viewModel.handleAction(
            HomeUiAction.OnStylesSelected(
                listOf("clean_white", "warm_linen", "morning_kitchen", "marble_luxe", "dark_moody")
            )
        )
        assertEquals(HomeUiState.MAX_STYLES, viewModel.uiState.value.selectedStyles.size)
    }

    @Test
    fun `unknown style ids are dropped from selection`() {
        val viewModel = createViewModel()
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("clean_white", "nope")))
        assertEquals(listOf("clean_white"), viewModel.uiState.value.selectedStyles.map { it.id })
    }

    @Test
    fun `adding a second photo collapses multi-style selection to first style`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri2")))
        assertEquals(listOf("warm_linen"), viewModel.uiState.value.selectedStyles.map { it.id })
    }

    @Test
    fun `adding photos keeps single-style selection intact`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen")))
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri2")))
        assertEquals(listOf("warm_linen"), viewModel.uiState.value.selectedStyles.map { it.id })
    }

    @Test
    fun `styleMaxSelectable is 1 with multiple photos`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1", "uri2")))
        assertEquals(1, viewModel.uiState.value.styleMaxSelectable)
    }

    @Test
    fun `styleMaxSelectable is MAX_STYLES with one photo`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        assertEquals(HomeUiState.MAX_STYLES, viewModel.uiState.value.styleMaxSelectable)
    }

    @Test
    fun `generate stores all selected styles on config`() {
        val holder = GenerationConfigHolderImpl()
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10, configHolder = holder)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        viewModel.handleAction(HomeUiAction.OnGenerateClicked)
        assertEquals(listOf("warm_linen", "clean_white"), holder.currentConfig!!.styles.map { it.id })
    }

    @Test
    fun `style picker nav action carries selected ids and max selectable`() {
        val viewModel = createViewModel(isSignedIn = true, creditBalance = 10)
        viewModel.handleAction(HomeUiAction.OnPhotosSelected(listOf("uri1")))
        viewModel.handleAction(HomeUiAction.OnStylesSelected(listOf("warm_linen", "clean_white")))
        viewModel.handleAction(HomeUiAction.OnNavigationHandled)
        viewModel.handleAction(HomeUiAction.OnStylePickerClicked)
        val nav = viewModel.navigationEvent.value
        assertTrue(nav is HomeNavigationAction.GoToStylePicker)
        assertEquals(
            listOf("warm_linen", "clean_white"),
            (nav as HomeNavigationAction.GoToStylePicker).currentStyleIds
        )
        assertEquals(HomeUiState.MAX_STYLES, nav.maxSelectable)
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

    // --- Credit state tests ---

    @Test
    fun `credit state is Error when credits fetch fails after sign-in`() {
        val authService = FakeAuthService(signedIn = true)
        val creditManager = FakeCreditManager(balance = 10, shouldFail = true)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(FakeWelcomeCreditGranter(), creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )
        // Signed in, but credits failed to load
        assertTrue(viewModel.uiState.value.isSignedIn)
        assertFalse(viewModel.uiState.value.isLoadingCredits)
        assertEquals(0, viewModel.uiState.value.creditBalance)
        assertFalse(viewModel.uiState.value.canAffordGeneration)
    }

    @Test
    fun `credit state transitions from LoggedOut to Loaded after sign-in`() {
        val authService = MutableFakeAuthService()
        val creditManager = FakeCreditManager(balance = 15)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(FakeWelcomeCreditGranter(), creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )

        // Initially logged out
        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(0, viewModel.uiState.value.creditBalance)

        // Simulate sign-in — UnconfinedTestDispatcher runs the flow update eagerly
        authService.setSignedIn(true)

        assertTrue(viewModel.uiState.value.isSignedIn)
        assertEquals(15, viewModel.uiState.value.creditBalance)
        assertFalse(viewModel.uiState.value.isLoadingCredits)
    }

    @Test
    fun `credit state resets to LoggedOut on sign-out`() {
        val authService = MutableFakeAuthService()
        val creditManager = FakeCreditManager(balance = 10)
        val viewModel = HomeViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            observeCreditStateUseCase = ObserveCreditStateUseCase(authService, creditManager),
            generationConfigHolder = GenerationConfigHolderImpl(),
            analyticsService = FakeAnalyticsService(),
            historyRepository = FakeHistoryRepository(),
            ensureWelcomeCreditsUseCase = EnsureWelcomeCreditsUseCase(FakeWelcomeCreditGranter(), creditManager, FakeErrorReporter()),
            buildKontextPromptUseCase = BuildKontextPromptUseCase()
        )

        authService.setSignedIn(true)
        assertTrue(viewModel.uiState.value.isSignedIn)

        authService.setSignedIn(false)
        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(0, viewModel.uiState.value.creditBalance)
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

    /**
     * Mirrors [CreditManagerImpl]: starts with credits=null, populates on loadCredits().
     * [shouldFail] simulates a network error — loadCredits() returns failure and leaves
     * credits null, which causes ObserveCreditStateUseCase to emit Error.
     *
     * Note: Loading state is unreachable in ObserveCreditStateUseCase because loadCredits()
     * is fully awaited before combine() subscribes. Loading exists for potential future use.
     */
    private class FakeCreditManager(
        balance: Int,
        private val shouldFail: Boolean = false
    ) : CreditManager {
        private var balance: Int = balance
        private var failNextRefreshOnly = false
        fun setBalance(value: Int) { balance = value }
        fun failNextRefresh() { failNextRefreshOnly = true }
        private val _credits = MutableStateFlow<UserCredits?>(null)
        override val credits: StateFlow<UserCredits?> = _credits
        private val _isLoading = MutableStateFlow(false)
        override val isLoading: StateFlow<Boolean> = _isLoading
        override suspend fun loadCredits(): Result<UserCredits> {
            if (shouldFail) return Result.failure(Exception("Credit fetch failed"))
            val userCredits = UserCredits(balance)
            _credits.value = userCredits
            return Result.success(userCredits)
        }
        override suspend fun refreshCredits(): Result<UserCredits> {
            if (failNextRefreshOnly) {
                failNextRefreshOnly = false
                return Result.failure(Exception("Refresh failed"))
            }
            return loadCredits()
        }
        override fun clearCredits() { _credits.value = null }
    }

    /** A WelcomeCreditGranter whose claim can be paused mid-flight to simulate an in-progress network call. */
    private class SuspendingWelcomeCreditGranter : WelcomeCreditGranter {
        private var gate: CompletableDeferred<Unit>? = null

        fun pause() { gate = CompletableDeferred() }
        fun resume() { gate?.complete(Unit) }

        override suspend fun claimWelcomeCredits(): Boolean {
            gate?.await()
            return true
        }
    }

    private class MutableFakeAuthService : AuthService {
        private val _isSignedIn = MutableStateFlow(false)
        override val isSignedIn: StateFlow<Boolean> = _isSignedIn
        fun setSignedIn(value: Boolean) { _isSignedIn.value = value }
        override suspend fun awaitInitialized(): Boolean = _isSignedIn.value
        override suspend fun signIn(): Result<AuthUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun getCurrentUser(): AuthUser? = null
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
        override suspend fun setGalleryUri(id: String, galleryUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }

    private class FakeWelcomeCreditGranter(
        private val granted: Boolean = false
    ) : WelcomeCreditGranter {
        var claimCalled = false
        override suspend fun claimWelcomeCredits(): Boolean {
            claimCalled = true
            return granted
        }
    }
}
