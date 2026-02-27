package com.middleton.studiosnap.feature.mainrestore.presentation.viewmodel

import com.middleton.studiosnap.core.data.cache.ImageCacheManager
import com.middleton.studiosnap.core.domain.service.FakeImagePersistenceService
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.RatingService
import com.middleton.studiosnap.core.domain.service.WatermarkService
import com.middleton.studiosnap.core.presentation.navigation.FakeNavigationStrategy
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.feature.mainrestore.domain.model.RestorationResult
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RestorationRepository
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RestorationStatusResponse
import com.middleton.studiosnap.feature.mainrestore.domain.usecase.SaveRestorationToGalleryUseCase
import com.middleton.studiosnap.feature.mainrestore.presentation.navigation.ResultNavigationAction
import com.middleton.studiosnap.feature.mainrestore.presentation.action.ResultUiAction
import com.middleton.studiosnap.feature.mainrestore.presentation.ui_state.ResultUiState
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest : BaseViewModelTest() {

    private lateinit var fakeNavigationStrategy: FakeNavigationStrategy<ResultNavigationAction>
    private lateinit var fakeRestorationRepository: FakeRestorationRepository
    private lateinit var fakeRecentRestorationsRepository: FakeResultRecentRestorationsRepo
    private lateinit var fakeGalleryRepository: FakeResultGalleryRepo
    private lateinit var fakeWatermarkService: FakeResultWatermarkService
    private lateinit var fakeUserPreferencesRepository: FakeResultUserPreferencesRepository
    private lateinit var fakeRatingService: FakeRatingService
    private lateinit var saveRestorationToGalleryUseCase: SaveRestorationToGalleryUseCase

    private val normalResult = RestorationResult(
        originalImagePath = "/tmp/original.jpg",
        restoredImagePath = "/tmp/restored.jpg",
        processingTimeSeconds = 5,
        restoreOptions = PhotoRestoreOptions(),
        creditsUsed = 1,
        isWatermarked = false
    )

    private val watermarkedResult = RestorationResult(
        originalImagePath = "/tmp/original.jpg",
        restoredImagePath = "/tmp/restored.jpg",
        processingTimeSeconds = 5,
        restoreOptions = PhotoRestoreOptions(),
        creditsUsed = 0,
        isWatermarked = true
    )

    @BeforeTest
    fun setup() {
        fakeNavigationStrategy = FakeNavigationStrategy()
        fakeRestorationRepository = FakeRestorationRepository()
        fakeRecentRestorationsRepository = FakeResultRecentRestorationsRepo()
        fakeGalleryRepository = FakeResultGalleryRepo()
        fakeWatermarkService = FakeResultWatermarkService()
        fakeUserPreferencesRepository = FakeResultUserPreferencesRepository()
        fakeRatingService = FakeRatingService()
        saveRestorationToGalleryUseCase = SaveRestorationToGalleryUseCase(
            galleryRepository = fakeGalleryRepository,
            recentRestorationsRepository = fakeRecentRestorationsRepository,
            imagePersistenceService = FakeImagePersistenceService()
        )
    }

    private fun createViewModel(): ResultViewModel {
        return ResultViewModel(
            restorationRepository = fakeRestorationRepository,
            navigationStrategy = fakeNavigationStrategy,
            imageCacheManager = ImageCacheManager(),
            saveRestorationToGalleryUseCase = saveRestorationToGalleryUseCase,
            galleryRepository = fakeGalleryRepository,
            recentRestorationsRepository = fakeRecentRestorationsRepository,
            watermarkService = fakeWatermarkService,
            analyticsService = com.middleton.studiosnap.core.domain.service.FakeAnalyticsService(),
            userPreferencesRepository = fakeUserPreferencesRepository,
            ratingService = fakeRatingService
        )
    }

    @Test
    fun `normal result should show success state`() = runTest {
        fakeRestorationRepository.emitResult(normalResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<ResultUiState.Success>(state)
        assertFalse(state.result.isWatermarked)
    }

    @Test
    fun `watermarked result should show success state with isWatermarked`() = runTest {
        fakeRestorationRepository.emitResult(watermarkedResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<ResultUiState.Success>(state)
        assertTrue(state.result.isWatermarked)
    }

    @Test
    fun `restoreAnother should navigate to main restore`() = runTest {
        fakeRestorationRepository.emitResult(normalResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(ResultUiAction.RestoreAnother)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(
            ResultNavigationAction.RestoreAnother,
            fakeNavigationStrategy.getLastNavigatedAction()
        )
    }

    @Test
    fun `unlockFullQuality should navigate to token purchase and pop result`() = runTest {
        fakeRestorationRepository.emitResult(watermarkedResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(ResultUiAction.UnlockFullQuality)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigationStrategy.hasNavigated())
        assertEquals(
            ResultNavigationAction.UnlockFullQuality,
            fakeNavigationStrategy.getLastNavigatedAction()
        )
    }

    @Test
    fun `watermarked result should save to persistent storage not gallery`() = runTest {
        fakeRestorationRepository.emitResult(watermarkedResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<ResultUiState.Success>(state)
        assertTrue(state.saveSuccess)
        assertFalse(fakeGalleryRepository.saveImageCalled, "Gallery save should be skipped for watermarked results")
        assertTrue(fakeRecentRestorationsRepository.savedRestorations.isNotEmpty())
    }

    @Test
    fun `normal result should auto-save to gallery`() = runTest {
        fakeRestorationRepository.emitResult(normalResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<ResultUiState.Success>(state)
        assertTrue(state.saveSuccess)
        assertTrue(fakeGalleryRepository.saveImageCalled)
    }

    @Test
    fun `share watermarked result applies watermark`() = runTest {
        fakeRestorationRepository.emitResult(watermarkedResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(ResultUiAction.Share)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeWatermarkService.timesApplied, "Watermark should be applied on share")
    }

    @Test
    fun `share non-watermarked result does not apply watermark`() = runTest {
        fakeRestorationRepository.emitResult(normalResult)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleAction(ResultUiAction.Share)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeWatermarkService.timesApplied, "Watermark should not be applied for paid result")
    }

    @Test
    fun `no result should show error state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<ResultUiState.Error>(state)
    }

    @Test
    fun `rating prompt triggered after second paid restoration`() = runTest {
        // First restoration - no prompt
        fakeRestorationRepository.emitResult(normalResult)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(fakeRatingService.reviewRequested)

        // Second restoration - prompt fires
        fakeRestorationRepository.emitResult(normalResult)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(fakeRatingService.reviewRequested)
    }

    @Test
    fun `rating prompt not triggered for watermarked restorations`() = runTest {
        // Two watermarked restorations - no prompt
        fakeRestorationRepository.emitResult(watermarkedResult)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRestorationRepository.emitResult(watermarkedResult)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fakeRatingService.reviewRequested)
    }
}

class FakeRestorationRepository : RestorationRepository {
    override var currentResult: RestorationResult? = null

    fun emitResult(result: RestorationResult?) {
        currentResult = result
    }

    override suspend fun createRestoration(imageBase64DataUri: String, prompt: String): Result<String> =
        Result.success("prediction-123")

    override suspend fun getRestorationStatus(predictionId: String): Result<RestorationStatusResponse> =
        Result.success(RestorationStatusResponse(status = "succeeded", outputUrl = "https://example.com/output.jpg"))

    override suspend fun createUpscale(imageUrl: String, scaleFactor: Int): Result<String> =
        Result.success("upscale-prediction-123")

    override suspend fun downloadRestoredImage(url: String): Result<ByteArray> =
        Result.success(ByteArray(0))

    override suspend fun downloadRestoredImageToFile(url: String): Result<String> =
        Result.success("/tmp/downloaded.jpg")

    override suspend fun downloadRestoredImageToFileWithProgress(
        url: String,
        onProgress: suspend (Float) -> Unit
    ): Result<String> = Result.success("/tmp/downloaded.jpg")

    override fun setResult(result: RestorationResult) {
        currentResult = result
    }

    override fun clearResult() {
        currentResult = null
    }

    override suspend fun getImageDimensions(filePath: String): Pair<Int, Int>? = Pair(1024, 768)
}

class FakeResultRecentRestorationsRepo : RecentRestorationsRepository {
    val savedRestorations = mutableListOf<RecentRestoration>()

    override fun observeRecentRestorations(limit: Int): Flow<List<RecentRestoration>> =
        flowOf(emptyList())

    override suspend fun getRecentRestorations(limit: Int): List<RecentRestoration> = emptyList()

    override suspend fun saveRestoration(restoration: RecentRestoration): Result<Unit> {
        savedRestorations.add(restoration)
        return Result.success(Unit)
    }

    override suspend fun removeRestoration(restorationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun hideRestoration(restorationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun clearAll(): Result<Unit> = Result.success(Unit)

    override suspend fun getRestoration(id: String): RecentRestoration? = null

    override suspend fun getWatermarkedRestorations(): List<RecentRestoration> = emptyList()

    override suspend fun unlockAllWatermarked(): Result<Unit> = Result.success(Unit)

    override suspend fun updateRestoredImagePath(id: String, newPath: String): Result<Unit> =
        Result.success(Unit)
}

class FakeResultGalleryRepo : GalleryRepository {
    var saveImageCalled = false

    override suspend fun saveImage(filePath: String, displayName: String): Result<String> {
        saveImageCalled = true
        return Result.success("gallery://fake/$displayName")
    }

    override suspend fun deleteImage(galleryUri: String): Result<Unit> = Result.success(Unit)

    override suspend fun imageExists(galleryUri: String): Boolean = true
}

class FakeResultWatermarkService : WatermarkService {
    var timesApplied = 0
        private set

    override suspend fun apply(inputPath: String, outputPath: String): Result<Unit> {
        timesApplied++
        return Result.success(Unit)
    }
}

class FakeResultUserPreferencesRepository : UserPreferencesRepository {
    private var completedRestorations = 0

    override fun observeHasUsedFreeTrial(): Flow<Boolean> = flowOf(false)
    override suspend fun hasUsedFreeTrial(): Boolean = false
    override suspend fun hasSeenPostTrialPaywall(): Boolean = false
    override suspend fun hasPurchasedCredits(): Boolean = false
    override suspend fun setHasUsedFreeTrial() {}
    override suspend fun setHasSeenPostTrialPaywall() {}
    override suspend fun setHasPurchasedCredits() {}
    override suspend fun incrementAndGetCompletedRestorations(): Int = ++completedRestorations
}

class FakeRatingService : RatingService {
    var reviewRequested = false
        private set

    override suspend fun requestReview() {
        reviewRequested = true
    }
}
