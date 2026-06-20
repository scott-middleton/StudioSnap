package com.middleton.studiosnap.core.domain.usecase

import com.middleton.studiosnap.core.domain.model.AuthUser
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.core.presentation.state.UserCreditLoadingState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCreditStateUseCaseTest : BaseViewModelTest() {

    private class FakeAuthService : AuthService {
        private val _isSignedIn = MutableStateFlow(false)
        override val isSignedIn: StateFlow<Boolean> = _isSignedIn
        fun setSignedIn(value: Boolean) { _isSignedIn.value = value }
        override suspend fun awaitInitialized(): Boolean = _isSignedIn.value
        override suspend fun signIn(): Result<AuthUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun getCurrentUser(): AuthUser? = null
    }

    private class GatedFakeCreditManager(private val balance: Int) : CreditManager {
        private val _credits = MutableStateFlow<UserCredits?>(null)
        override val credits: StateFlow<UserCredits?> = _credits
        private val _isLoading = MutableStateFlow(false)
        override val isLoading: StateFlow<Boolean> = _isLoading

        val gate = CompletableDeferred<Unit>()

        override suspend fun loadCredits(): Result<UserCredits> {
            _isLoading.value = true
            gate.await()
            val userCredits = UserCredits(balance)
            _credits.value = userCredits
            _isLoading.value = false
            return Result.success(userCredits)
        }

        override suspend fun refreshCredits(): Result<UserCredits> = loadCredits()
        override fun clearCredits() { _credits.value = null }
    }

    @Test
    fun `emits Loading immediately after sign-in while loadCredits is in flight`() = runTest(context = testDispatcher) {
        val authService = FakeAuthService()
        val creditManager = GatedFakeCreditManager(balance = 15)
        val useCase = ObserveCreditStateUseCase(authService, creditManager)

        val states = mutableListOf<UserCreditLoadingState>()
        val job = launch { useCase().collect { states.add(it) } }

        assertEquals(listOf<UserCreditLoadingState>(UserCreditLoadingState.LoggedOut), states)

        authService.setSignedIn(true)
        assertEquals(
            listOf<UserCreditLoadingState>(UserCreditLoadingState.LoggedOut, UserCreditLoadingState.Loading),
            states
        )

        creditManager.gate.complete(Unit)
        assertEquals(
            listOf<UserCreditLoadingState>(
                UserCreditLoadingState.LoggedOut,
                UserCreditLoadingState.Loading,
                UserCreditLoadingState.Loaded(UserCredits(15))
            ),
            states
        )

        job.cancel()
    }
}
