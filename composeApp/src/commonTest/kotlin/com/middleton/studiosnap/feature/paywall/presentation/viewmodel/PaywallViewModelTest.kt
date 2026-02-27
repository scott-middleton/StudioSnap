package com.middleton.studiosnap.feature.paywall.presentation.viewmodel

import com.middleton.studiosnap.core.domain.service.FakeErrorReporter
import com.middleton.studiosnap.core.domain.service.FakeImagePersistenceService
import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.AuthProvider
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.PurchaseRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.navigation.FakeNavigationStrategy
import com.middleton.studiosnap.feature.auth.domain.usecase.SignInUseCase
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import com.middleton.studiosnap.feature.mainrestore.domain.usecase.UnlockRestorationsUseCase
import com.middleton.studiosnap.feature.paywall.presentation.action.PaywallUiAction
import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.TokenPack
import com.revenuecat.purchases.kmp.models.StoreProduct
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
class PaywallViewModelTest : BaseViewModelTest() {

    private lateinit var fakeNavigationStrategy: FakeNavigationStrategy<PaywallNavigationAction>
    private lateinit var fakePurchaseRepository: FakePaywallPurchaseRepository
    private lateinit var fakeCreditManager: FakePaywallCreditManager
    private lateinit var fakeAuthService: FakePaywallAuthService
    private lateinit var fakeUnlockRestorationsUseCase: UnlockRestorationsUseCase
    private lateinit var fakeUserPreferencesRepository: FakePaywallUserPreferencesRepo
    private lateinit var fakeRecentRestorationsRepository: FakePaywallRecentRestorationsRepo
    private lateinit var fakeGalleryRepository: FakePaywallGalleryRepository

    @BeforeTest
    fun setup() {
        fakeNavigationStrategy = FakeNavigationStrategy()
        fakePurchaseRepository = FakePaywallPurchaseRepository()
        fakeCreditManager = FakePaywallCreditManager()
        fakeAuthService = FakePaywallAuthService()
        fakeUserPreferencesRepository = FakePaywallUserPreferencesRepo()
        fakeRecentRestorationsRepository = FakePaywallRecentRestorationsRepo()
        fakeGalleryRepository = FakePaywallGalleryRepository()
        fakeUnlockRestorationsUseCase = UnlockRestorationsUseCase(
            recentRestorationsRepository = fakeRecentRestorationsRepository,
            galleryRepository = fakeGalleryRepository,
            imagePersistenceService = FakeImagePersistenceService(),
            errorReporter = FakeErrorReporter()
        )
    }

    private fun createViewModel(
        unlockShouldFail: Boolean = false,
        signedIn: Boolean = true,
        initialCredits: Int = 5
    ): PaywallViewModel {
        fakeAuthService = FakePaywallAuthService(signedIn = signedIn)
        fakeCreditManager = FakePaywallCreditManager(initialCredits = initialCredits)
        val unlockUseCase = if (unlockShouldFail) {
            UnlockRestorationsUseCase(
                recentRestorationsRepository = FakePaywallRecentRestorationsRepo(unlockShouldFail = true),
                galleryRepository = fakeGalleryRepository,
                imagePersistenceService = FakeImagePersistenceService(),
                errorReporter = FakeErrorReporter()
            )
        } else {
            fakeUnlockRestorationsUseCase
        }
        return PaywallViewModel(
            purchaseRepository = fakePurchaseRepository,
            creditManager = fakeCreditManager,
            navigationStrategy = fakeNavigationStrategy,
            authService = fakeAuthService,
            signInUseCase = SignInUseCase(fakeAuthService, fakeCreditManager),
            unlockRestorationsUseCase = unlockUseCase,
            userPreferencesRepository = fakeUserPreferencesRepository,
            analyticsService = com.middleton.studiosnap.core.domain.service.FakeAnalyticsService()
        )
    }

    @Test
    fun `successful purchase dismisses paywall`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(PaywallNavigationAction.PurchaseComplete, fakeNavigationStrategy.getLastNavigatedAction())
    }

    @Test
    fun `successful purchase sets hasPurchasedCredits`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeUserPreferencesRepository.hasPurchasedCredits)
    }

    @Test
    fun `unlock failure after purchase still dismisses paywall`() = runTest {
        val viewModel = createViewModel(unlockShouldFail = true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            fakeNavigationStrategy.navigatedActions.contains(PaywallNavigationAction.PurchaseComplete),
            "Paywall should navigate to MainRestore even when unlock fails"
        )
    }

    @Test
    fun `unlock failure after purchase shows error message`() = runTest {
        val viewModel = createViewModel(unlockShouldFail = true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.error, "Error should be shown when unlock fails")
    }

    @Test
    fun `successful unlock after purchase shows no error`() = runTest {
        val viewModel = createViewModel(unlockShouldFail = false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNull(state.error, "No error should be shown when unlock succeeds")
    }

    @Test
    fun `sign-in for purchase with existing credits still executes purchase`() = runTest {
        val viewModel = createViewModel(signedIn = false, initialCredits = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should trigger sign-in, not purchase yet
        assertTrue(viewModel.uiState.first().showSignIn)

        // Simulate successful sign-in
        viewModel.handleAction(PaywallUiAction.OnSignInResult(true))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should proceed with purchase (PurchaseComplete from successful buy)
        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(PaywallNavigationAction.PurchaseComplete, fakeNavigationStrategy.getLastNavigatedAction())
    }

    @Test
    fun `sign-in for purchase with no credits executes purchase`() = runTest {
        val viewModel = createViewModel(signedIn = false, initialCredits = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.OnSignInResult(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(PaywallNavigationAction.PurchaseComplete, fakeNavigationStrategy.getLastNavigatedAction())
    }

    @Test
    fun `sign-in via button with credits navigates to main restore`() = runTest {
        val viewModel = createViewModel(signedIn = false, initialCredits = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        // Sign in via button (not purchase)
        viewModel.handleAction(PaywallUiAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.OnSignInResult(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(PaywallNavigationAction.PurchaseComplete, fakeNavigationStrategy.getLastNavigatedAction())
    }

    @Test
    fun `sign-in via button with no credits stays on paywall`() = runTest {
        val viewModel = createViewModel(signedIn = false, initialCredits = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.OnSignInResult(true))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should NOT navigate — stays on paywall
        assertFalse(fakeNavigationStrategy.hasNavigated())
        // Should show sign-in success feedback
        assertTrue(viewModel.uiState.first().signInSuccess)
    }

    @Test
    fun `DismissSignInSuccess resets signInSuccess flag`() = runTest {
        val viewModel = createViewModel(signedIn = false, initialCredits = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleAction(PaywallUiAction.OnSignInResult(true))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().signInSuccess)

        viewModel.handleAction(PaywallUiAction.DismissSignInSuccess)
        assertFalse(viewModel.uiState.first().signInSuccess)
    }

    @Test
    fun `ConfirmPurchase when not signed in should set isSigningIn true`() = runTest {
        val viewModel = createViewModel(signedIn = false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().isSigningIn)
    }

    @Test
    fun `SignIn action should set isSigningIn true`() = runTest {
        val viewModel = createViewModel(signedIn = false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().isSigningIn)
    }

    @Test
    fun `OnSignInResult should clear isSigningIn`() = runTest {
        val viewModel = createViewModel(signedIn = false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isSigningIn)

        viewModel.handleAction(PaywallUiAction.OnSignInResult(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().isSigningIn)
    }

    @Test
    fun `purchase failure does not dismiss paywall`() = runTest {
        fakePurchaseRepository.purchaseShouldFail = true
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(PaywallUiAction.SelectPack(fakePurchaseRepository.fakePack))
        viewModel.handleAction(PaywallUiAction.ConfirmPurchase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fakeNavigationStrategy.hasNavigated())
        assertNotNull(viewModel.uiState.first().error)
    }
}

// --- Fakes ---

class FakePaywallPurchaseRepository : PurchaseRepository {
    var purchaseShouldFail = false

    val fakePack = TokenPack(
        storeProduct = object : StoreProduct {
            override val id = "restorer_ai_pack_popular"
            override val type = com.revenuecat.purchases.kmp.models.ProductType.INAPP
            override val price = com.revenuecat.purchases.kmp.models.Price("$9.99", 999000, "USD")
            override val title = "20 Credits"
            override val period = null
            override val subscriptionOptions = null
            override val defaultOption = null
            override val discounts: List<com.revenuecat.purchases.kmp.models.StoreProductDiscount> = emptyList()
            override val introductoryDiscount = null
            override val purchasingData = object : com.revenuecat.purchases.kmp.models.PurchasingData {
                override val productId = "restorer_ai_pack_popular"
                override val productType = com.revenuecat.purchases.kmp.models.ProductType.INAPP
            }
            override val presentedOfferingContext = null
            override val category: com.revenuecat.purchases.kmp.models.ProductCategory? = null
            override val localizedDescription: String? = null
        },
        grantedCredits = 20
    )

    override suspend fun purchaseTokenPack(storeProduct: StoreProduct): Result<UserCredits> {
        return if (purchaseShouldFail) {
            Result.failure(Exception("Purchase failed"))
        } else {
            Result.success(UserCredits(tokenCount = 20))
        }
    }

    override suspend fun getAvailableTokenPacks(): Result<List<StoreProduct>> =
        Result.success(emptyList())

    override suspend fun restorePurchases(): Result<UserCredits> =
        Result.success(UserCredits(tokenCount = 0))
}

class FakePaywallCreditManager(initialCredits: Int = 5) : CreditManager {
    private val _credits = MutableStateFlow<UserCredits?>(UserCredits(tokenCount = initialCredits))
    override val credits: StateFlow<UserCredits?> = _credits

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    fun setCredits(count: Int) { _credits.value = UserCredits(tokenCount = count) }

    override suspend fun loadCredits(): Result<UserCredits> =
        Result.success(_credits.value ?: UserCredits(tokenCount = 0))

    override suspend fun refreshCredits(): Result<UserCredits> = loadCredits()

    override fun clearCredits() { _credits.value = null }
}

class FakePaywallAuthService(signedIn: Boolean = true) : AuthService {
    private val _isSignedIn = MutableStateFlow(signedIn)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn

    fun setSignedIn(value: Boolean) { _isSignedIn.value = value }

    override suspend fun awaitInitialized(): Boolean = true

    override suspend fun signIn(): Result<AuthUser> {
        _isSignedIn.value = true
        return Result.success(AuthUser("id", "test@test.com", "Test", AuthProvider.GOOGLE))
    }

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun getCurrentUser(): AuthUser =
        AuthUser("id", "test@test.com", "Test", AuthProvider.GOOGLE)
}

class FakePaywallUserPreferencesRepo : UserPreferencesRepository {
    var hasPurchasedCredits = false

    override fun observeHasUsedFreeTrial(): Flow<Boolean> = flowOf(true)
    override suspend fun hasUsedFreeTrial(): Boolean = true
    override suspend fun hasSeenPostTrialPaywall(): Boolean = true
    override suspend fun hasPurchasedCredits(): Boolean = hasPurchasedCredits
    override suspend fun setHasUsedFreeTrial() {}
    override suspend fun setHasSeenPostTrialPaywall() {}
    override suspend fun setHasPurchasedCredits() { hasPurchasedCredits = true }
    override suspend fun incrementAndGetCompletedRestorations(): Int = 0
}

class FakePaywallRecentRestorationsRepo(
    private val unlockShouldFail: Boolean = false
) : RecentRestorationsRepository {
    override fun observeRecentRestorations(limit: Int) = flowOf(emptyList<com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration>())
    override suspend fun getRecentRestorations(limit: Int) = emptyList<com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration>()
    override suspend fun saveRestoration(restoration: com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration) = Result.success(Unit)
    override suspend fun removeRestoration(restorationId: String) = Result.success(Unit)
    override suspend fun hideRestoration(restorationId: String) = Result.success(Unit)
    override suspend fun clearAll() = Result.success(Unit)
    override suspend fun getRestoration(id: String) = null
    override suspend fun getWatermarkedRestorations() = emptyList<com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration>()
    override suspend fun unlockAllWatermarked(): Result<Unit> =
        if (unlockShouldFail) Result.failure(Exception("Unlock failed")) else Result.success(Unit)
    override suspend fun updateRestoredImagePath(id: String, newPath: String) = Result.success(Unit)
}

class FakePaywallGalleryRepository : com.middleton.studiosnap.core.domain.repository.GalleryRepository {
    override suspend fun saveImage(filePath: String, displayName: String) = Result.success("gallery://fake/$displayName")
    override suspend fun deleteImage(galleryUri: String) = Result.success(Unit)
    override suspend fun imageExists(galleryUri: String) = true
}

