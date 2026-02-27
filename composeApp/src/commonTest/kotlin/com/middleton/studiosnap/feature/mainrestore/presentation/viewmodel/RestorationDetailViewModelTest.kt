package com.middleton.studiosnap.feature.mainrestore.presentation.viewmodel

import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.WatermarkService
import com.middleton.studiosnap.core.presentation.navigation.FakeNavigationStrategy
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import com.middleton.studiosnap.feature.mainrestore.domain.usecase.DeleteRestorationUseCase
import com.middleton.studiosnap.feature.mainrestore.presentation.action.RestorationDetailUiAction
import com.middleton.studiosnap.feature.mainrestore.presentation.navigation.RestorationDetailNavigationAction
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
class RestorationDetailViewModelTest : BaseViewModelTest() {

    private lateinit var fakeRepo: FakeDetailRestorationsRepo
    private lateinit var fakeGalleryRepo: FakeDetailGalleryRepo
    private lateinit var fakeWatermarkService: FakeDetailWatermarkService
    private lateinit var fakeNav: FakeNavigationStrategy<RestorationDetailNavigationAction>
    private val testRestoration = RecentRestoration(
        id = "test-id",
        originalImagePath = "original/test.jpg",
        restoredImagePath = "restored/test.jpg",
        restoreDate = 1000000L,
        restoreOptions = PhotoRestoreOptions(),
        tokenCost = 1,
        isWatermarked = false
    )

    @BeforeTest
    fun setup() {
        fakeRepo = FakeDetailRestorationsRepo(testRestoration)
        fakeGalleryRepo = FakeDetailGalleryRepo()
        fakeWatermarkService = FakeDetailWatermarkService()
        fakeNav = FakeNavigationStrategy()
    }

    private fun createViewModel(restorationId: String = "test-id") =
        RestorationDetailViewModel(
            restorationId = restorationId,
            recentRestorationsRepository = fakeRepo,
            deleteRestorationUseCase = DeleteRestorationUseCase(fakeGalleryRepo, fakeRepo),
            watermarkService = fakeWatermarkService,
            navigationStrategy = fakeNav,
            analyticsService = com.middleton.studiosnap.core.domain.service.FakeAnalyticsService()
        )

    @Test
    fun `init loads restoration and sets state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("test-id", state.restorationId)
        assertEquals("restored/test.jpg", state.imagePath)
        assertFalse(state.isWatermarked)
    }

    @Test
    fun `missing restoration navigates back`() = runTest {
        val viewModel = createViewModel(restorationId = "nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            RestorationDetailNavigationAction.NavigateBack,
            fakeNav.getLastNavigatedAction()
        )
    }

    @Test
    fun `delete shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationDetailUiAction.Delete)

        assertTrue(viewModel.uiState.first().showDeleteDialog)
    }

    @Test
    fun `dismiss delete dialog hides it`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationDetailUiAction.Delete)
        viewModel.handleAction(RestorationDetailUiAction.DismissDeleteDialog)

        assertFalse(viewModel.uiState.first().showDeleteDialog)
    }

    @Test
    fun `confirm delete removes restoration and navigates back`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationDetailUiAction.Delete)
        viewModel.handleAction(RestorationDetailUiAction.ConfirmDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepo.deletedIds.contains("test-id"))
        assertEquals(
            RestorationDetailNavigationAction.NavigateBack,
            fakeNav.getLastNavigatedAction()
        )
    }

    @Test
    fun `toggle fullscreen flips state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationDetailUiAction.ToggleFullScreen)
        assertTrue(viewModel.uiState.first().isFullScreen)

        viewModel.handleAction(RestorationDetailUiAction.ToggleFullScreen)
        assertFalse(viewModel.uiState.first().isFullScreen)
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(RestorationDetailUiAction.OnBack)

        assertEquals(
            RestorationDetailNavigationAction.NavigateBack,
            fakeNav.getLastNavigatedAction()
        )
    }
}

// --- Fakes ---

class FakeDetailRestorationsRepo(
    private val restoration: RecentRestoration? = null
) : RecentRestorationsRepository {
    val deletedIds = mutableListOf<String>()

    override fun observeRecentRestorations(limit: Int): Flow<List<RecentRestoration>> =
        MutableStateFlow(listOfNotNull(restoration))
    override suspend fun getRecentRestorations(limit: Int) = listOfNotNull(restoration)
    override suspend fun saveRestoration(restoration: RecentRestoration) = Result.success(Unit)
    override suspend fun removeRestoration(restorationId: String): Result<Unit> {
        deletedIds.add(restorationId)
        return Result.success(Unit)
    }
    override suspend fun hideRestoration(restorationId: String) = Result.success(Unit)
    override suspend fun clearAll() = Result.success(Unit)
    override suspend fun getRestoration(id: String) = if (restoration?.id == id) restoration else null
    override suspend fun getWatermarkedRestorations() = emptyList<RecentRestoration>()
    override suspend fun unlockAllWatermarked() = Result.success(Unit)
    override suspend fun updateRestoredImagePath(id: String, newPath: String) = Result.success(Unit)
}

class FakeDetailGalleryRepo : GalleryRepository {
    override suspend fun saveImage(filePath: String, displayName: String) = Result.success("gallery://fake")
    override suspend fun deleteImage(galleryUri: String) = Result.success(Unit)
    override suspend fun imageExists(galleryUri: String) = true
}

class FakeDetailWatermarkService : WatermarkService {
    override suspend fun apply(inputPath: String, outputPath: String): Result<Unit> = Result.success(Unit)
}
