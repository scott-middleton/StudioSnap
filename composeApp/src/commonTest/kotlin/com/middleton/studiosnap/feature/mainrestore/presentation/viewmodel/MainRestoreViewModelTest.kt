package com.middleton.studiosnap.feature.mainrestore.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.AuthProvider
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerResult
import com.middleton.studiosnap.core.presentation.navigation.FakeNavigationStrategy
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import com.middleton.studiosnap.feature.mainrestore.domain.usecase.CleanUpRestorationsUseCase
import com.middleton.studiosnap.feature.mainrestore.presentation.action.MainRestoreUiAction
import com.middleton.studiosnap.feature.mainrestore.presentation.ui_state.MainRestoreUiState
import com.middleton.studiosnap.feature.mainrestore.presentation.navigation.MainRestoreNavigationAction
import com.middleton.studiosnap.feature.mainrestore.presentation.ui_state.UserCreditLoadingState
import kotlinx.datetime.Clock
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainRestoreViewModelTest : BaseViewModelTest() {

    private lateinit var fakeNavigationStrategy: FakeNavigationStrategy<MainRestoreNavigationAction>
    private lateinit var fakeCreditManager: FakeCreditManager
    private lateinit var fakeAuthService: FakeAuthService
    private lateinit var fakeRecentRestorationsRepository: FakeRecentRestorationsRepo
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepo
    private lateinit var fakeCleanUpRestorationsUseCase: CleanUpRestorationsUseCase
    private lateinit var viewModel: MainRestoreViewModel

    @BeforeTest
    fun setup() {
        fakeNavigationStrategy = FakeNavigationStrategy()
        fakeCreditManager = FakeCreditManager()
        fakeAuthService = FakeAuthService()
        fakeRecentRestorationsRepository = FakeRecentRestorationsRepo()
        fakeUserPreferencesRepository = FakeUserPreferencesRepo()
        fakeCleanUpRestorationsUseCase = CleanUpRestorationsUseCase(
            galleryRepository = FakeGalleryRepo(),
            recentRestorationsRepository = fakeRecentRestorationsRepository
        )
        viewModel = createViewModel()
    }

    private fun createViewModel(
        navigationStrategy: FakeNavigationStrategy<MainRestoreNavigationAction> = fakeNavigationStrategy,
        creditManager: CreditManager = fakeCreditManager,
        authService: AuthService = fakeAuthService,
        recentRestorationsRepository: RecentRestorationsRepository = fakeRecentRestorationsRepository,
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        cleanUpRestorationsUseCase: CleanUpRestorationsUseCase = fakeCleanUpRestorationsUseCase,
        analyticsService: com.middleton.studiosnap.core.domain.service.AnalyticsService = com.middleton.studiosnap.core.domain.service.FakeAnalyticsService()
    ): MainRestoreViewModel {
        return MainRestoreViewModel(
            navigationStrategy = navigationStrategy,
            creditManager = creditManager,
            authService = authService,
            recentRestorationsRepository = recentRestorationsRepository,
            userPreferencesRepository = userPreferencesRepository,
            cleanUpRestorationsUseCase = cleanUpRestorationsUseCase,
            analyticsService = analyticsService
        )
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.userCreditLoadingState is UserCreditLoadingState.Loaded)
        assertNull(state.selectedImage)
        assertFalse(state.isPhotoSelectionSheetVisible)
        assertFalse(state.isLoading)
        assertEquals(3, state.recentRestorations.size)
        assertFalse(state.canStartRestore)
    }

    @Test
    fun `handleAction SelectPhoto should set loading state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.SelectPhoto)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `handleAction OnPhotoSelected should update state correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val imageResult = ImagePickerResult(uri = "content://test/image.jpg")

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(imageResult))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(imageResult, state.selectedImage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `handleAction ShowPhotoSelectionSheet should show sheet`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.ShowPhotoSelectionSheet)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isPhotoSelectionSheetVisible)
    }

    @Test
    fun `handleAction HidePhotoSelectionSheet should hide sheet`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.ShowPhotoSelectionSheet)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.HidePhotoSelectionSheet)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isPhotoSelectionSheetVisible)
    }

    @Test
    fun `handleAction StartRestore when canStartRestore should navigate to processing`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.first()
        assertTrue(stateBefore.canStartRestore)

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        val lastAction = fakeNavigationStrategy.getLastNavigatedAction()
        assertTrue(lastAction is MainRestoreNavigationAction.NavigateToProcessing)
        val processingAction = lastAction as MainRestoreNavigationAction.NavigateToProcessing
        assertEquals("content://test/image.jpg", processingAction.imageUri)
        assertFalse(processingAction.isFreeRestoration)
    }

    @Test
    fun `handleAction StartRestore when no photo selected should not navigate`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.first()
        assertFalse(initialState.canStartRestore)

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fakeNavigationStrategy.hasNavigated())
    }

    @Test
    fun `handleAction StartRestore with insufficient credits should navigate to paywall`() = runTest {
        fakeCreditManager.setCredits(0)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        val lastAction = fakeNavigationStrategy.getLastNavigatedAction()
        assertEquals(MainRestoreNavigationAction.NavigateToTokenPurchase, lastAction)
    }

    @Test
    fun `handleAction NavigationAction should call navigation strategy`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val navigationAction = MainRestoreNavigationAction.NavigateToTokenPurchase

        viewModel.handleAction(MainRestoreUiAction.NavigationAction(navigationAction))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(navigationAction, fakeNavigationStrategy.getLastNavigatedAction())
    }

    @Test
    fun `handleAction OpenCamera should delegate to photo selection`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OpenCamera)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `handleAction PhotoPickerCancelled should clear loading state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.SelectPhoto)
        testDispatcher.scheduler.advanceUntilIdle()

        val loadingState = viewModel.uiState.first()
        assertTrue(loadingState.isLoading)

        viewModel.handleAction(MainRestoreUiAction.PhotoPickerCancelled)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.selectedImage)
    }

    @Test
    fun `recent restorations should be properly initialized from mock data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.recentRestorations.size)

        val firstRestoration = state.recentRestorations.first()
        assertEquals("rest_1", firstRestoration.id)
        assertEquals("original_1.jpg", firstRestoration.originalImagePath)
        assertEquals("restored_1.jpg", firstRestoration.restoredImagePath)
        assertEquals("thumb_1.jpg", firstRestoration.thumbnailPath)
        assertEquals(1, firstRestoration.tokenCost)
    }

    @Test
    fun `when user is signed in credits should be loaded`() = runTest {
        val freshCreditManager = FakeCreditManager().apply { setCredits(10) }
        val freshAuthService = FakeAuthService().apply { setSignedIn(true) }
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            creditManager = freshCreditManager,
            authService = freshAuthService,
            recentRestorationsRepository = freshRecentRepo,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        kotlinx.coroutines.delay(100)

        val state = testViewModel.uiState.first()
        val loadingState = state.userCreditLoadingState
        assertTrue(loadingState is UserCreditLoadingState.Loaded, "Expected Loaded but got $loadingState")
        val credits = loadingState.userCredits.tokenCount
        assertTrue(credits == 10 || credits == 12, "Expected 10 or 12 credits but got $credits")
    }

    @Test
    fun `when user is not signed in should show logged out state`() = runTest {
        val freshAuthService = FakeAuthService().apply { setSignedIn(false) }
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            creditManager = FakeCreditManager(),
            authService = freshAuthService,
            recentRestorationsRepository = freshRecentRepo,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        kotlinx.coroutines.delay(100)

        val state = testViewModel.uiState.first()
        assertEquals(UserCreditLoadingState.LoggedOut, state.userCreditLoadingState)
    }

    @Test
    fun `handleAction OnCreditChipTapped when logged out should navigate to sign in`() = runTest {
        val freshAuthService = FakeAuthService().apply { setSignedIn(false) }
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            creditManager = FakeCreditManager(),
            authService = freshAuthService,
            recentRestorationsRepository = freshRecentRepo,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        kotlinx.coroutines.delay(100)

        testViewModel.handleAction(MainRestoreUiAction.OnCreditChipTapped)

        val state = testViewModel.uiState.first()
        assertTrue(state.showSignIn, "Credit chip tap when logged out should show sign-in dialog")
    }

    @Test
    fun `hasUsedFreeTrial false should set free trial mode`() = runTest {
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo(visibleRestorations = emptyList())
        val freshPrefs = FakeUserPreferencesRepo(hasUsedFreeTrial = false)

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            recentRestorationsRepository = freshRecentRepo,
            userPreferencesRepository = freshPrefs,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = testViewModel.uiState.first()
        assertTrue(state.isFreeTrialMode)
    }

    @Test
    fun `hasUsedFreeTrial true should not set free trial mode`() = runTest {
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()
        val freshPrefs = FakeUserPreferencesRepo(hasUsedFreeTrial = true)

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            recentRestorationsRepository = freshRecentRepo,
            userPreferencesRepository = freshPrefs,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = testViewModel.uiState.first()
        assertFalse(state.isFreeTrialMode)
    }

    @Test
    fun `StartRestore should set isRestoring true when navigating to processing`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().isRestoring)
    }

    @Test
    fun `isRestoring blocks canStartRestore`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().canStartRestore)

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().canStartRestore)
    }

    @Test
    fun `StartRestore should keep selected image after navigation to avoid layout jank`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.selectedImage, "Selected image should be kept to prevent layout jank during transition")
    }

    @Test
    fun `free trial mode should reactively update when preference changes`() = runTest {
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo(visibleRestorations = emptyList())
        val freshPrefs = FakeUserPreferencesRepo(hasUsedFreeTrial = false)

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            recentRestorationsRepository = freshRecentRepo,
            userPreferencesRepository = freshPrefs,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(testViewModel.uiState.first().isFreeTrialMode)

        freshPrefs.hasUsedFreeTrial = true
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(testViewModel.uiState.first().isFreeTrialMode, "Free trial mode should reactively update when hasUsedFreeTrial changes")
    }

    @Test
    fun `free trial mode StartRestore should navigate with isFreeRestoration true`() = runTest {
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo(visibleRestorations = emptyList())
        val freshPrefs = FakeUserPreferencesRepo(hasUsedFreeTrial = false)

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            recentRestorationsRepository = freshRecentRepo,
            userPreferencesRepository = freshPrefs,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        testDispatcher.scheduler.advanceUntilIdle()
        freshNavStrategy.clear()

        testViewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        testViewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(freshNavStrategy.hasNavigated())
        val lastAction = freshNavStrategy.getLastNavigatedAction()
        assertTrue(lastAction is MainRestoreNavigationAction.NavigateToProcessing)
        val processingAction = lastAction as MainRestoreNavigationAction.NavigateToProcessing
        assertTrue(processingAction.isFreeRestoration)
    }

    // --- User Prompt tests ---

    @Test
    fun `UpdateUserPrompt should update state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.UpdateUserPrompt("The dress is blue"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("The dress is blue", viewModel.uiState.first().userPrompt)
    }

    @Test
    fun `UpdateUserPrompt should enforce character limit`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val longPrompt = "a".repeat(MainRestoreUiState.MAX_USER_PROMPT_LENGTH + 10)
        viewModel.handleAction(MainRestoreUiAction.UpdateUserPrompt(longPrompt))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.first().userPrompt, "Prompt exceeding max length should be rejected")
    }

    @Test
    fun `UpdateUserPrompt at exact limit should be accepted`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val exactPrompt = "a".repeat(MainRestoreUiState.MAX_USER_PROMPT_LENGTH)
        viewModel.handleAction(MainRestoreUiAction.UpdateUserPrompt(exactPrompt))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MainRestoreUiState.MAX_USER_PROMPT_LENGTH, viewModel.uiState.first().userPrompt.length)
    }

    @Test
    fun `ToggleUserPromptExpanded should toggle state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().isUserPromptExpanded)

        viewModel.handleAction(MainRestoreUiAction.ToggleUserPromptExpanded)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isUserPromptExpanded)

        viewModel.handleAction(MainRestoreUiAction.ToggleUserPromptExpanded)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.first().isUserPromptExpanded)
    }

    @Test
    fun `StartRestore should pass user prompt to navigation`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNavigationStrategy.clear()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.UpdateUserPrompt("Make the car red"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        val lastAction = fakeNavigationStrategy.getLastNavigatedAction()
        assertTrue(lastAction is MainRestoreNavigationAction.NavigateToProcessing)
        assertEquals("Make the car red", (lastAction as MainRestoreNavigationAction.NavigateToProcessing).userPrompt)
    }

    @Test
    fun `StartRestore with empty prompt should pass empty string`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        fakeNavigationStrategy.clear()

        viewModel.handleAction(MainRestoreUiAction.OnPhotoSelected(ImagePickerResult(uri = "content://test/image.jpg")))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(MainRestoreUiAction.StartRestore)
        testDispatcher.scheduler.advanceUntilIdle()

        val lastAction = fakeNavigationStrategy.getLastNavigatedAction()
        assertTrue(lastAction is MainRestoreNavigationAction.NavigateToProcessing)
        assertEquals("", (lastAction as MainRestoreNavigationAction.NavigateToProcessing).userPrompt)
    }

    @Test
    fun `OnSignInResult success should reset showSignIn`() = runTest {
        val freshAuthService = FakeAuthService().apply { setSignedIn(false) }
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            authService = freshAuthService,
            recentRestorationsRepository = freshRecentRepo,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        kotlinx.coroutines.delay(100)

        // Trigger sign-in
        testViewModel.handleAction(MainRestoreUiAction.OnCreditChipTapped)
        assertTrue(testViewModel.uiState.first().showSignIn)

        // Sign-in succeeds
        testViewModel.handleAction(MainRestoreUiAction.OnSignInResult(true))
        assertFalse(testViewModel.uiState.first().showSignIn)
    }

    @Test
    fun `OnSignInResult failure should reset showSignIn`() = runTest {
        val freshAuthService = FakeAuthService().apply { setSignedIn(false) }
        val freshNavStrategy = FakeNavigationStrategy<MainRestoreNavigationAction>()
        val freshRecentRepo = FakeRecentRestorationsRepo()

        val testViewModel = createViewModel(
            navigationStrategy = freshNavStrategy,
            authService = freshAuthService,
            recentRestorationsRepository = freshRecentRepo,
            cleanUpRestorationsUseCase = CleanUpRestorationsUseCase(FakeGalleryRepo(), freshRecentRepo)
        )

        kotlinx.coroutines.delay(100)

        // Trigger sign-in
        testViewModel.handleAction(MainRestoreUiAction.OnCreditChipTapped)
        assertTrue(testViewModel.uiState.first().showSignIn)

        // Sign-in fails
        testViewModel.handleAction(MainRestoreUiAction.OnSignInResult(false))
        assertFalse(testViewModel.uiState.first().showSignIn)
    }
}

class FakeCreditManager : CreditManager {
    private val _credits = MutableStateFlow<UserCredits?>(UserCredits(tokenCount = 12))
    override val credits: StateFlow<UserCredits?> = _credits

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    fun setCredits(amount: Int) {
        _credits.value = UserCredits(tokenCount = amount)
    }

    override suspend fun loadCredits(): Result<UserCredits> {
        val userCredits = _credits.value ?: UserCredits(tokenCount = 12)
        _credits.value = userCredits
        return Result.success(userCredits)
    }

    override suspend fun refreshCredits(): Result<UserCredits> {
        return loadCredits()
    }

    override fun clearCredits() {
        _credits.value = null
    }
}

class FakeAuthService : AuthService {
    private val _isSignedIn = MutableStateFlow(true)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn

    private var currentUser: AuthUser? = AuthUser(
        id = "test-user-id",
        email = "test@example.com",
        displayName = "Test User",
        provider = AuthProvider.GOOGLE
    )

    fun setSignedIn(signedIn: Boolean) {
        _isSignedIn.value = signedIn
        currentUser = if (signedIn) {
            AuthUser(
                id = "test-user-id",
                email = "test@example.com",
                displayName = "Test User",
                provider = AuthProvider.GOOGLE
            )
        } else {
            null
        }
    }

    override suspend fun awaitInitialized(): Boolean = _isSignedIn.value

    override suspend fun signIn(): Result<AuthUser> {
        return currentUser?.let { Result.success(it) }
            ?: Result.failure(Exception("Not signed in"))
    }

    override suspend fun signOut(): Result<Unit> {
        _isSignedIn.value = false
        currentUser = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): AuthUser? = currentUser
}

class FakeRecentRestorationsRepo(
    private val visibleRestorations: List<RecentRestoration>? = null
) : RecentRestorationsRepository {
    private val fakeRestorations = listOf(
        RecentRestoration(
            id = "rest_1",
            originalImagePath = "original_1.jpg",
            restoredImagePath = "restored_1.jpg",
            thumbnailPath = "thumb_1.jpg",
            restoreDate = Clock.System.now().toEpochMilliseconds(),
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        ),
        RecentRestoration(
            id = "rest_2",
            originalImagePath = "original_2.jpg",
            restoredImagePath = "restored_2.jpg",
            thumbnailPath = "thumb_2.jpg",
            restoreDate = Clock.System.now().toEpochMilliseconds(),
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        ),
        RecentRestoration(
            id = "rest_3",
            originalImagePath = "original_3.jpg",
            restoredImagePath = "restored_3.jpg",
            thumbnailPath = "thumb_3.jpg",
            restoreDate = Clock.System.now().toEpochMilliseconds(),
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        )
    )

    override fun observeRecentRestorations(limit: Int): Flow<List<RecentRestoration>> {
        val list = visibleRestorations ?: fakeRestorations
        return flowOf(list.take(limit))
    }

    override suspend fun getRecentRestorations(limit: Int): List<RecentRestoration> {
        val list = visibleRestorations ?: fakeRestorations
        return list.take(limit)
    }

    override suspend fun saveRestoration(restoration: RecentRestoration): Result<Unit> =
        Result.success(Unit)

    override suspend fun removeRestoration(restorationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun hideRestoration(restorationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun clearAll(): Result<Unit> = Result.success(Unit)

    override suspend fun getRestoration(id: String): RecentRestoration? =
        fakeRestorations.find { it.id == id }

    override suspend fun getWatermarkedRestorations(): List<RecentRestoration> = emptyList()

    override suspend fun unlockAllWatermarked(): Result<Unit> = Result.success(Unit)

    override suspend fun updateRestoredImagePath(id: String, newPath: String): Result<Unit> =
        Result.success(Unit)
}

class FakeUserPreferencesRepo(
    hasUsedFreeTrial: Boolean = true,
    var hasSeenPostTrialPaywall: Boolean = true,
    var hasPurchasedCredits: Boolean = false
) : UserPreferencesRepository {
    private val _hasUsedFreeTrial = MutableStateFlow(hasUsedFreeTrial)
    var hasUsedFreeTrial: Boolean
        get() = _hasUsedFreeTrial.value
        set(value) { _hasUsedFreeTrial.value = value }

    override fun observeHasUsedFreeTrial(): Flow<Boolean> = _hasUsedFreeTrial
    override suspend fun hasUsedFreeTrial(): Boolean = _hasUsedFreeTrial.value
    override suspend fun hasSeenPostTrialPaywall(): Boolean = hasSeenPostTrialPaywall
    override suspend fun hasPurchasedCredits(): Boolean = hasPurchasedCredits
    override suspend fun setHasUsedFreeTrial() { _hasUsedFreeTrial.value = true }
    override suspend fun setHasSeenPostTrialPaywall() { hasSeenPostTrialPaywall = true }
    override suspend fun setHasPurchasedCredits() { hasPurchasedCredits = true }
    override suspend fun incrementAndGetCompletedRestorations(): Int = 0
}

class FakeGalleryRepo : GalleryRepository {
    override suspend fun saveImage(filePath: String, displayName: String): Result<String> =
        Result.success("gallery://fake/$displayName")

    override suspend fun deleteImage(galleryUri: String): Result<Unit> = Result.success(Unit)

    override suspend fun imageExists(galleryUri: String): Boolean = true
}
