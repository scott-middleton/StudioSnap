package com.middleton.studiosnap.core.domain.usecase

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.WelcomeCreditGranter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnsureWelcomeCreditsUseCaseTest {

    @Test
    fun `claims welcome credits then refreshes balance`() = kotlinx.coroutines.test.runTest {
        val granter = FakeWelcomeCreditGranter(granted = true)
        val creditManager = FakeCreditManager(balance = 1)
        val useCase = EnsureWelcomeCreditsUseCase(granter, creditManager)

        val result = useCase()

        assertTrue(granter.claimCalled)
        assertTrue(creditManager.refreshCalled)
        assertEquals(1, result.getOrNull()?.amount)
    }

    @Test
    fun `claim failure is non-fatal and still refreshes balance`() = kotlinx.coroutines.test.runTest {
        val granter = FakeWelcomeCreditGranter(shouldThrow = true)
        val creditManager = FakeCreditManager(balance = 0)
        val useCase = EnsureWelcomeCreditsUseCase(granter, creditManager)

        val result = useCase()

        assertTrue(creditManager.refreshCalled)
        assertEquals(0, result.getOrNull()?.amount)
    }

    @Test
    fun `returns failure when refresh fails`() = kotlinx.coroutines.test.runTest {
        val granter = FakeWelcomeCreditGranter(granted = false)
        val creditManager = FakeCreditManager(balance = 0, shouldFailRefresh = true)
        val useCase = EnsureWelcomeCreditsUseCase(granter, creditManager)

        val result = useCase()

        assertTrue(result.isFailure)
    }

    private class FakeWelcomeCreditGranter(
        private val granted: Boolean = false,
        private val shouldThrow: Boolean = false
    ) : WelcomeCreditGranter {
        var claimCalled = false

        override suspend fun claimWelcomeCredits(): Boolean {
            claimCalled = true
            if (shouldThrow) throw Exception("Network error")
            return granted
        }
    }

    private class FakeCreditManager(
        private val balance: Int,
        private val shouldFailRefresh: Boolean = false
    ) : CreditManager {
        var refreshCalled = false
        private val _credits = MutableStateFlow<UserCredits?>(null)
        override val credits: StateFlow<UserCredits?> = _credits
        private val _isLoading = MutableStateFlow(false)
        override val isLoading: StateFlow<Boolean> = _isLoading

        override suspend fun loadCredits(): Result<UserCredits> = refreshCredits()

        override suspend fun refreshCredits(): Result<UserCredits> {
            refreshCalled = true
            if (shouldFailRefresh) return Result.failure(Exception("Refresh failed"))
            val userCredits = UserCredits(balance)
            _credits.value = userCredits
            return Result.success(userCredits)
        }

        override fun clearCredits() {
            _credits.value = null
        }
    }
}
