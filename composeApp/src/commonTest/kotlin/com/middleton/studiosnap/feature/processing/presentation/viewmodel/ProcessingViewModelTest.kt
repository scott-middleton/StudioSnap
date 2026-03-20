package com.middleton.studiosnap.feature.processing.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationError
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.processing.domain.usecase.BatchProgress
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerateBatchPreviewsUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GeneratePreviewUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.processing.presentation.action.ProcessingUiAction
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingStatus
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProcessingViewModelTest : BaseViewModelTest() {

    private val testStyle = Style(
        id = "clean_white",
        displayName = UiText.DynamicString("Clean White"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "Place the product on a pure white background"
    )

    private val testPhoto = ProductPhoto(id = "photo_1", localUri = "content://photo1", width = 1024, height = 768)
    private val testPhoto2 = ProductPhoto(id = "photo_2", localUri = "content://photo2", width = 800, height = 600)

    private val testConfig = GenerationConfig(
        photos = listOf(testPhoto),
        style = testStyle,
        shadow = false,
        reflection = false,
        exportFormat = ExportFormat.DEFAULT,
        quality = GenerationQuality.STANDARD,
        batchId = "test-batch-id"
    )

    @Test
    fun `null config shows error state`() {
        val vm = createViewModel(config = null)
        assertIs<ProcessingUiState.Error>(vm.uiState.value)
    }

    @Test
    fun `single photo completes and navigates to results`() {
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(resultsHolder = resultsHolder)

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertIs<ProcessingNavigationAction.GoToResults>(vm.navigationEvent.value)
        assertEquals(1, resultsHolder.currentResults?.size)
    }

    @Test
    fun `multi-photo batch stores all results`() {
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2))
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            config = twoPhotoConfig,
            resultsHolder = resultsHolder
        )

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertEquals(2, resultsHolder.currentResults?.size)
    }

    @Test
    fun `api failure still completes batch with failure results`() {
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            generationRepo = FakeGenerationRepository(shouldFail = true),
            resultsHolder = resultsHolder
        )

        // Batch still completes — individual failures are handled gracefully
        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        val results = resultsHolder.currentResults
        assertEquals(1, results?.size)
        assertIs<GenerationResult.Failure>(results?.first())
    }

    @Test
    fun `unexpected exception shows error state`() {
        // Flow itself throws — not a graceful API failure
        val throwingBatchUseCase = ThrowingBatchUseCase(RuntimeException("Unexpected crash"))
        val vm = createViewModelWithBatchUseCase(batchUseCase = throwingBatchUseCase)

        assertIs<ProcessingUiState.Error>(vm.uiState.value)
    }

    @Test
    fun `unexpected exception logs analytics`() {
        val analytics = FakeAnalyticsService()
        val throwingBatchUseCase = ThrowingBatchUseCase(RuntimeException("Crash"))
        createViewModelWithBatchUseCase(
            batchUseCase = throwingBatchUseCase,
            analyticsService = analytics
        )

        assertTrue(analytics.hasEvent(AnalyticsEvents.BATCH_GENERATION_FAILED))
    }

    @Test
    fun `successful batch logs analytics with counts`() {
        val analytics = FakeAnalyticsService()
        createViewModel(analyticsService = analytics)

        assertTrue(analytics.hasEvent(AnalyticsEvents.BATCH_GENERATION_COMPLETED))
        val params = analytics.loggedEvents
            .first { it.first == AnalyticsEvents.BATCH_GENERATION_COMPLETED }.second
        assertEquals("1", params["total"])
        assertEquals("1", params["success"])
        assertEquals("0", params["failure"])
    }

    @Test
    fun `mixed results batch reports correct success and failure counts`() {
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2))
        val analytics = FakeAnalyticsService()
        val repo = FakeGenerationRepository(failOnIndex = 1) // second photo fails
        createViewModel(
            config = twoPhotoConfig,
            generationRepo = repo,
            analyticsService = analytics
        )

        val params = analytics.loggedEvents
            .first { it.first == AnalyticsEvents.BATCH_GENERATION_COMPLETED }.second
        assertEquals("2", params["total"])
        assertEquals("1", params["success"])
        assertEquals("1", params["failure"])
    }

    @Test
    fun `credits deducted upfront equal photo count`() {
        val creditDeductor = FakeCreditDeductor()
        val errorReporter = FakeErrorReporter()
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2))
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor)
        ProcessingViewModel(
            generationConfigHolder = FakeGenerationConfigHolder(twoPhotoConfig),
            generationResultsHolder = FakeGenerationResultsHolder(),
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = FakeAnalyticsService(),
            completionDelayMs = 0L
        )

        assertEquals(2, creditDeductor.deductCalled)
    }

    @Test
    fun `failed photos trigger credit refund`() {
        val creditDeductor = FakeCreditDeductor()
        val errorReporter = FakeErrorReporter()
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2))
        val repo = FakeGenerationRepository(failOnIndex = 1)
        val generatePreview = GeneratePreviewUseCase(repo, FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor)
        val resultsHolder = FakeGenerationResultsHolder()
        ProcessingViewModel(
            generationConfigHolder = FakeGenerationConfigHolder(twoPhotoConfig),
            generationResultsHolder = resultsHolder,
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = FakeAnalyticsService(),
            completionDelayMs = 0L
        )

        assertEquals(1, creditDeductor.refundCalled)
        assertEquals(1, resultsHolder.refundedCredits)
    }

    @Test
    fun `deduction failure shows error state`() {
        val failingDeductor = FakeCreditDeductor(deductShouldFail = true)
        val errorReporter = FakeErrorReporter()
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, failingDeductor)
        val vm = createViewModelWithBatchUseCase(batchUseCase = batchUseCase)

        assertIs<ProcessingUiState.Error>(vm.uiState.value)
    }

    @Test
    fun `retry restarts processing`() {
        var callCount = 0
        val repo = object : FakeGenerationRepository() {
            override suspend fun generateImage(
                photo: ProductPhoto, style: Style, shadow: Boolean,
                reflection: Boolean, exportFormat: ExportFormat, quality: GenerationQuality,
                onProgress: (suspend (Float) -> Unit)?
            ): Result<GenerationResult.Success> {
                callCount++
                return super.generateImage(photo, style, shadow, reflection, exportFormat, quality, onProgress)
            }
        }

        val vm = createViewModel(generationRepo = repo)
        val firstCount = callCount

        vm.handleAction(ProcessingUiAction.OnRetryClicked)
        assertTrue(callCount > firstCount)
    }

    @Test
    fun `navigation handled clears event`() {
        val vm = createViewModel()

        assertIs<ProcessingNavigationAction.GoToResults>(vm.navigationEvent.value)
        vm.handleAction(ProcessingUiAction.OnNavigationHandled)
        assertNull(vm.navigationEvent.value)
    }

    @Test
    fun `onProgress callback is wired end-to-end from batch use case to repository`() {
        // Verifies that the onPhotoProgress callback passed to GenerateBatchPreviewsUseCase
        // propagates all the way through GeneratePreviewUseCase → GenerationRepository.
        val repo = ProgressEmittingRepository(listOf(0.1f, 0.55f, 0.9f))
        val vm = createViewModel(generationRepo = repo)

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertEquals(listOf(0.1f, 0.55f, 0.9f), repo.reportedProgress)
    }

    @Test
    fun `history saves successful results`() {
        val historyRepo = FakeHistoryRepository()
        createViewModel(historyRepo = historyRepo)

        assertEquals(1, historyRepo.savedResults.size)
    }

    // --- Factory ---

    private fun createViewModel(
        config: GenerationConfig? = testConfig,
        resultsHolder: GenerationResultsHolder = FakeGenerationResultsHolder(),
        generationRepo: GenerationRepository = FakeGenerationRepository(),
        historyRepo: HistoryRepository = FakeHistoryRepository(),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ): ProcessingViewModel {
        val configHolder = FakeGenerationConfigHolder(config)
        val errorReporter = FakeErrorReporter()
        val generatePreview = GeneratePreviewUseCase(generationRepo, historyRepo, errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, FakeCreditDeductor())

        return ProcessingViewModel(
            generationConfigHolder = configHolder,
            generationResultsHolder = resultsHolder,
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = analyticsService,
            completionDelayMs = 0L
        )
    }

    private fun createViewModelWithBatchUseCase(
        config: GenerationConfig? = testConfig,
        resultsHolder: GenerationResultsHolder = FakeGenerationResultsHolder(),
        batchUseCase: GenerateBatchPreviewsUseCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), FakeErrorReporter()),
            FakeCreditDeductor()
        ),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ): ProcessingViewModel {
        return ProcessingViewModel(
            generationConfigHolder = FakeGenerationConfigHolder(config),
            generationResultsHolder = resultsHolder,
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = analyticsService,
            completionDelayMs = 0L
        )
    }

    // --- Fakes ---

    private class FakeGenerationConfigHolder(
        override var currentConfig: GenerationConfig? = null
    ) : GenerationConfigHolder

    private class FakeGenerationResultsHolder : GenerationResultsHolder {
        override var currentResults: List<GenerationResult>? = null
        override var refundedCredits: Int = 0
    }

    private class FakeCreditDeductor(
        private val deductShouldFail: Boolean = false
    ) : CreditDeductor {
        var deductCalled = 0
        var refundCalled = 0

        override suspend fun deductCredits(amount: Int, reason: String): Result<UserCredits> {
            deductCalled += amount
            return if (deductShouldFail) Result.failure(RuntimeException("Insufficient credits"))
            else Result.success(UserCredits(100 - amount))
        }

        override suspend fun refundCredits(amount: Int, reason: String): Result<UserCredits> {
            refundCalled += amount
            return Result.success(UserCredits(100))
        }
    }

    private open class FakeGenerationRepository(
        private val shouldFail: Boolean = false,
        private val failOnIndex: Int = -1
    ) : GenerationRepository {
        private var counter = 0

        override suspend fun generateImage(
            photo: ProductPhoto, style: Style, shadow: Boolean,
            reflection: Boolean, exportFormat: ExportFormat, quality: GenerationQuality,
            onProgress: (suspend (Float) -> Unit)?
        ): Result<GenerationResult.Success> {
            val index = counter++
            if (shouldFail || index == failOnIndex) {
                return Result.failure(RuntimeException("API failure"))
            }
            return Result.success(
                GenerationResult.Success(
                    generationId = "gen_$index",
                    inputPhoto = photo,
                    previewUri = "preview_$index.jpg",
                    style = style,
                    createdAt = 1000L * index
                )
            )
        }

        override suspend fun downloadFullRes(generationId: String) =
            Result.success("/path/full_$generationId.jpg")
    }

    /** Invokes onProgress with a fixed list of values, then succeeds. */
    private class ProgressEmittingRepository(
        private val progressToEmit: List<Float>
    ) : FakeGenerationRepository() {
        val reportedProgress = mutableListOf<Float>()

        override suspend fun generateImage(
            photo: ProductPhoto, style: Style, shadow: Boolean,
            reflection: Boolean, exportFormat: ExportFormat, quality: GenerationQuality,
            onProgress: (suspend (Float) -> Unit)?
        ): Result<GenerationResult.Success> {
            progressToEmit.forEach { value ->
                reportedProgress.add(value)
                onProgress?.invoke(value)
            }
            return super.generateImage(photo, style, shadow, reflection, exportFormat, quality, onProgress)
        }
    }

    private class FakeHistoryRepository : HistoryRepository {
        val savedResults = mutableListOf<GenerationResult.Success>()

        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getSessions() = flowOf(emptyList<com.middleton.studiosnap.feature.history.domain.model.HistorySession>())
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {
            savedResults.add(result)
        }
        override suspend fun saveAll(results: List<GenerationResult.Success>) {
            savedResults.addAll(results)
        }
        override suspend fun getById(id: String) = savedResults.find { it.generationId == id }
        override suspend fun delete(id: String) {
            savedResults.removeAll { it.generationId == id }
        }
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }

    private class FakeErrorReporter : ErrorReporter {
        override fun recordException(exception: Throwable) {}
    }

    /**
     * A batch use case wrapper that throws from the flow (simulating unexpected errors
     * that aren't handled by GeneratePreviewUseCase's result folding).
     */
    private class ThrowingBatchUseCase(
        private val exception: Throwable
    ) : GenerateBatchPreviewsUseCase(
        GeneratePreviewUseCase(
            FakeGenerationRepository(), FakeHistoryRepository(), FakeErrorReporter()
        ),
        FakeCreditDeductor()
    ) {
        override fun invoke(
            config: GenerationConfig,
            onPhotoProgress: (suspend (photoIndex: Int, progress: Float) -> Unit)?
        ): Flow<BatchProgress> = flow {
            throw exception
        }
    }
}
