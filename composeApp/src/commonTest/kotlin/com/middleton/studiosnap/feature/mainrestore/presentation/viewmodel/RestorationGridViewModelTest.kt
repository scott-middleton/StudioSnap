package com.middleton.studiosnap.feature.mainrestore.presentation.viewmodel

import com.middleton.studiosnap.core.presentation.navigation.FakeNavigationStrategy
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import com.middleton.studiosnap.feature.mainrestore.presentation.action.RestorationGridUiAction
import com.middleton.studiosnap.feature.mainrestore.presentation.navigation.RestorationGridNavigationAction
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestorationGridViewModelTest : BaseViewModelTest() {

    private lateinit var fakeRepo: FakeGridRestorationsRepo
    private lateinit var fakeNav: FakeNavigationStrategy<RestorationGridNavigationAction>

    @BeforeTest
    fun setup() {
        fakeRepo = FakeGridRestorationsRepo()
        fakeNav = FakeNavigationStrategy()
    }

    private fun createViewModel() = RestorationGridViewModel(fakeRepo, fakeNav, com.middleton.studiosnap.core.domain.service.FakeAnalyticsService())

    @Test
    fun `init observes restorations and sets loading false`() = runTest {
        val restorations = listOf(testRestoration("1"), testRestoration("2"))
        fakeRepo.restorations.value = restorations

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.restorations.size)
    }

    @Test
    fun `empty restorations sets empty list`() = runTest {
        fakeRepo.restorations.value = emptyList()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.restorations.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `clicking restoration navigates to detail`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationGridUiAction.OnRestorationClick("abc"))

        assertEquals(
            RestorationGridNavigationAction.NavigateToDetail("abc"),
            fakeNav.getLastNavigatedAction()
        )
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationGridUiAction.OnBack)

        assertEquals(
            RestorationGridNavigationAction.NavigateBack,
            fakeNav.getLastNavigatedAction()
        )
    }

    private fun testRestoration(id: String) = RecentRestoration(
        id = id,
        originalImagePath = "original/$id.jpg",
        restoredImagePath = "restored/$id.jpg",
        restoreDate = 1000000L,
        restoreOptions = PhotoRestoreOptions(),
        tokenCost = 1
    )
}

// --- Fake ---

class FakeGridRestorationsRepo : RecentRestorationsRepository {
    val restorations = MutableStateFlow<List<RecentRestoration>>(emptyList())

    override fun observeRecentRestorations(limit: Int): Flow<List<RecentRestoration>> = restorations
    override suspend fun getRecentRestorations(limit: Int) = restorations.value
    override suspend fun saveRestoration(restoration: RecentRestoration) = Result.success(Unit)
    override suspend fun removeRestoration(restorationId: String) = Result.success(Unit)
    override suspend fun hideRestoration(restorationId: String) = Result.success(Unit)
    override suspend fun clearAll() = Result.success(Unit)
    override suspend fun getRestoration(id: String) = restorations.value.find { it.id == id }
    override suspend fun getWatermarkedRestorations() = emptyList<RecentRestoration>()
    override suspend fun unlockAllWatermarked() = Result.success(Unit)
    override suspend fun updateRestoredImagePath(id: String, newPath: String) = Result.success(Unit)
}
