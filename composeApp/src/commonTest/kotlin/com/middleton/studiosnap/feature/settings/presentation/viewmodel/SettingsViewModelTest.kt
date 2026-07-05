package com.middleton.studiosnap.feature.settings.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.AuthProvider
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.domain.service.RatingService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.settings.presentation.action.SettingsUiAction
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.purchases.PurchasesIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest : BaseViewModelTest() {

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

        vm.handleAction(SettingsUiAction.OnNavigationHandled)
        assertNull(vm.navigationEvent.value)
    }

    // --- Sign Out Tests ---

    @Test
    fun `sign out clicked shows confirmation dialog`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnSignOutClicked)
        assertTrue(vm.uiState.value.showSignOutConfirmation)
    }

    @Test
    fun `sign out dismissed hides confirmation dialog`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnSignOutClicked)
        assertTrue(vm.uiState.value.showSignOutConfirmation)

        vm.handleAction(SettingsUiAction.OnSignOutDismissed)
        assertFalse(vm.uiState.value.showSignOutConfirmation)
    }

    @Test
    fun `sign out confirmed navigates to splash`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)
        assertIs<SettingsNavigationAction.GoToSplashAfterSignOut>(vm.navigationEvent.value)
    }

    @Test
    fun `sign out confirmed clears purchases identity`() {
        val fakeIdentifier = FakePurchasesIdentifier()
        val vm = createViewModel(isSignedIn = true, purchasesIdentifier = fakeIdentifier)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)
        assertTrue(fakeIdentifier.identityCleared)
    }

    @Test
    fun `sign out confirmed clears cached credits`() {
        val fakeCreditManager = FakeCreditManager()
        val vm = createViewModel(isSignedIn = true, creditManager = fakeCreditManager)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)
        assertTrue(fakeCreditManager.clearCreditsCalled)
    }

    @Test
    fun `sign out confirmed shows error on failure`() {
        val failingAuth = FakeAuthService(signedIn = true, signOutFails = true)
        val vm = createViewModel(authService = failingAuth)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)

        assertNull(vm.navigationEvent.value)
        assertEquals("Sign out failed", vm.uiState.value.signOutError)
        assertFalse(vm.uiState.value.isSigningOut)
    }

    @Test
    fun `sign out confirmed does not clear credits on failure`() {
        val failingAuth = FakeAuthService(signedIn = true, signOutFails = true)
        val fakeCreditManager = FakeCreditManager()
        val vm = createViewModel(authService = failingAuth, creditManager = fakeCreditManager)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)
        assertFalse(fakeCreditManager.clearCreditsCalled)
    }

    @Test
    fun `sign out error dismissed clears error`() {
        val failingAuth = FakeAuthService(signedIn = true, signOutFails = true)
        val vm = createViewModel(authService = failingAuth)
        vm.handleAction(SettingsUiAction.OnSignOutConfirmed)
        assertEquals("Sign out failed", vm.uiState.value.signOutError)

        vm.handleAction(SettingsUiAction.OnSignOutErrorDismissed)
        assertNull(vm.uiState.value.signOutError)
    }

    // --- Delete Account Tests ---

    @Test
    fun `delete account clicked shows confirmation dialog`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnDeleteAccountClicked)
        assertTrue(vm.uiState.value.showDeleteAccountConfirmation)
    }

    @Test
    fun `delete account dismissed hides confirmation dialog`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnDeleteAccountClicked)
        assertTrue(vm.uiState.value.showDeleteAccountConfirmation)

        vm.handleAction(SettingsUiAction.OnDeleteAccountDismissed)
        assertFalse(vm.uiState.value.showDeleteAccountConfirmation)
    }

    @Test
    fun `delete account confirmed shows success dialog without navigating`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)

        assertFalse(vm.uiState.value.showDeleteAccountConfirmation)
        assertTrue(vm.uiState.value.showDeleteAccountSuccess)
        assertNull(vm.navigationEvent.value)
    }

    @Test
    fun `delete account success dismissed navigates to splash`() {
        val vm = createViewModel(isSignedIn = true)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)
        assertTrue(vm.uiState.value.showDeleteAccountSuccess)

        vm.handleAction(SettingsUiAction.OnDeleteAccountSuccessDismissed)
        assertFalse(vm.uiState.value.showDeleteAccountSuccess)
        assertIs<SettingsNavigationAction.GoToSplashAfterSignOut>(vm.navigationEvent.value)
    }

    @Test
    fun `delete account confirmed clears cached credits`() {
        val fakeCreditManager = FakeCreditManager()
        val vm = createViewModel(isSignedIn = true, creditManager = fakeCreditManager)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)
        assertTrue(fakeCreditManager.clearCreditsCalled)
    }

    @Test
    fun `delete account confirmed shows error on failure`() {
        val failingAuth = FakeAuthService(signedIn = true, deleteAccountFails = true)
        val vm = createViewModel(authService = failingAuth)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)

        assertNull(vm.navigationEvent.value)
        assertEquals("Delete failed", vm.uiState.value.deleteAccountError)
    }

    @Test
    fun `delete account confirmed does not clear credits on failure`() {
        val failingAuth = FakeAuthService(signedIn = true, deleteAccountFails = true)
        val fakeCreditManager = FakeCreditManager()
        val vm = createViewModel(authService = failingAuth, creditManager = fakeCreditManager)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)
        assertFalse(fakeCreditManager.clearCreditsCalled)
    }

    @Test
    fun `delete account error dismissed clears error`() {
        val failingAuth = FakeAuthService(signedIn = true, deleteAccountFails = true)
        val vm = createViewModel(authService = failingAuth)
        vm.handleAction(SettingsUiAction.OnDeleteAccountConfirmed)
        assertEquals("Delete failed", vm.uiState.value.deleteAccountError)

        vm.handleAction(SettingsUiAction.OnDeleteAccountErrorDismissed)
        assertNull(vm.uiState.value.deleteAccountError)
    }

    // --- Rate App Test ---

    @Test
    fun `rate app clicked opens store review`() {
        val fakeRating = FakeRatingService()
        val vm = createViewModel(ratingService = fakeRating)
        vm.handleAction(SettingsUiAction.OnRateAppClicked)
        assertTrue(fakeRating.storeReviewOpened)
    }

    // --- Factory ---

    private fun createViewModel(
        isSignedIn: Boolean = false,
        creditsFlow: Flow<UserCredits> = flowOf(UserCredits(10)),
        authService: AuthService = FakeAuthService(isSignedIn),
        ratingService: RatingService = FakeRatingService(),
        purchasesIdentifier: PurchasesIdentifier = FakePurchasesIdentifier(),
        creditManager: CreditManager = FakeCreditManager()
    ): SettingsViewModel {
        return SettingsViewModel(
            creditQueries = FakeCreditQueries(creditsFlow),
            authService = authService,
            analyticsService = FakeAnalyticsService(),
            ratingService = ratingService,
            purchasesIdentifier = purchasesIdentifier,
            creditManager = creditManager
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

    private class FakeCreditManager : CreditManager {
        var clearCreditsCalled = false
            private set

        override val credits: StateFlow<UserCredits?> = MutableStateFlow(UserCredits(10))
        override val isLoading: StateFlow<Boolean> = MutableStateFlow(false)
        override suspend fun loadCredits() = Result.success(UserCredits(10))
        override suspend fun refreshCredits() = Result.success(UserCredits(10))
        override fun clearCredits() {
            clearCreditsCalled = true
        }
    }

    private class FakeAuthService(
        signedIn: Boolean = false,
        private val signOutFails: Boolean = false,
        private val deleteAccountFails: Boolean = false
    ) : AuthService {
        override val isSignedIn: StateFlow<Boolean> = MutableStateFlow(signedIn)
        override suspend fun awaitInitialized() = isSignedIn.value
        override suspend fun signIn() = Result.success(AuthUser(id = "user_1", email = null, displayName = null, provider = AuthProvider.GOOGLE))
        override suspend fun signOut(): Result<Unit> {
            return if (signOutFails) {
                Result.failure(Exception("Sign out failed"))
            } else {
                Result.success(Unit)
            }
        }
        override suspend fun deleteAccount(): Result<Unit> {
            return if (deleteAccountFails) {
                Result.failure(Exception("Delete failed"))
            } else {
                Result.success(Unit)
            }
        }
        override suspend fun getCurrentUser() = if (isSignedIn.value) AuthUser(id = "user_1", email = null, displayName = null, provider = AuthProvider.GOOGLE) else null
    }

    private class FakeRatingService : RatingService {
        var storeReviewOpened = false
            private set

        override suspend fun requestReview() {}
        override suspend fun openStoreReviewPage() {
            storeReviewOpened = true
        }
    }

    private class FakePurchasesIdentifier : PurchasesIdentifier {
        var identityCleared = false
            private set

        override suspend fun identifyUser(userId: String) = Result.success(Unit)
        override fun clearIdentity() {
            identityCleared = true
        }
    }
}
