package com.middleton.studiosnap.feature.processing.presentation.viewmodel

import com.middleton.studiosnap.core.domain.exception.InsufficientCreditsException
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
import com.middleton.studiosnap.feature.processing.domain.usecase.BatchResumeState
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

    private val testStyle2 = Style(
        id = "warm_linen",
        displayName = UiText.DynamicString("Warm Linen"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "Warm linen background"
    )

    private val testPhoto = ProductPhoto(id = "photo_1", localUri = "content://photo1", width = 1024, height = 768)
    private val testPhoto2 = ProductPhoto(id = "photo_2", localUri = "content://photo2", width = 800, height = 600)

    /** Builds the cartesian product of units (photo-major) with each style's prompt resolved. */
    private fun unitsFor(
        photos: List<ProductPhoto>,
        styles: List<Style>
    ): List<GenerationConfig.GenerationUnit> = photos.flatMap { photo ->
        styles.map { style ->
            GenerationConfig.GenerationUnit(photo, style, style.kontextPrompt)
        }
    }

    private val testConfig = GenerationConfig(
        photos = listOf(testPhoto),
        units = unitsFor(listOf(testPhoto), listOf(testStyle)),
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
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            config = twoPhotoConfig,
            resultsHolder = resultsHolder
        )

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertEquals(2, resultsHolder.currentResults?.size)
    }

    @Test
    fun `api failure on every unit shows all-failed error instead of navigating`() {
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            generationRepo = FakeGenerationRepository(shouldFail = true),
            resultsHolder = resultsHolder
        )

        // Nothing succeeded — Results would be a dead end, so stay on Processing
        assertIs<ProcessingUiState.Error.AllFailed>(vm.uiState.value)
        assertNull(vm.navigationEvent.value)
        assertNull(resultsHolder.currentResults)
    }

    @Test
    fun `all-failed multi-unit batch reports total refunded credits without navigating`() {
        val twoPhotoConfig = testConfig.copy(
            photos = listOf(testPhoto, testPhoto2),
            units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle))
        )
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            config = twoPhotoConfig,
            generationRepo = FakeGenerationRepository(shouldFail = true),
            resultsHolder = resultsHolder
        )

        assertEquals(ProcessingUiState.Error.AllFailed(refundedCredits = 2), vm.uiState.value)
        assertNull(vm.navigationEvent.value)
        assertNull(resultsHolder.currentResults)
    }

    @Test
    fun `mixed batch with at least one success still navigates to results`() {
        val twoPhotoConfig = testConfig.copy(
            photos = listOf(testPhoto, testPhoto2),
            units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle))
        )
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = createViewModel(
            config = twoPhotoConfig,
            generationRepo = FakeGenerationRepository(failOnIndex = 1),
            resultsHolder = resultsHolder
        )

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertIs<ProcessingNavigationAction.GoToResults>(vm.navigationEvent.value)
        assertEquals(2, resultsHolder.currentResults?.size)
    }

    @Test
    fun `retry after all-failed re-runs every unit from scratch`() {
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository(shouldFail = true)
        val twoPhotoConfig = testConfig.copy(
            photos = listOf(testPhoto, testPhoto2),
            units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle))
        )
        val vm = createViewModelWithDeductor(
            config = twoPhotoConfig,
            generationRepo = repo,
            creditDeductor = creditDeductor
        )

        assertIs<ProcessingUiState.Error.AllFailed>(vm.uiState.value)
        assertEquals(2, creditDeductor.deductCalled)

        vm.handleAction(ProcessingUiAction.OnRetryClicked)

        // A resume would skip both recorded failures and re-run nothing —
        // the all-failed retry must instead re-attempt every unit.
        assertEquals(4, creditDeductor.deductCalled)
        assertEquals(2, repo.callCountByPhotoId[testPhoto.id])
        assertEquals(2, repo.callCountByPhotoId[testPhoto2.id])
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
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
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
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor, FakeErrorReporter())
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
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
        val repo = FakeGenerationRepository(failOnIndex = 1)
        val generatePreview = GeneratePreviewUseCase(repo, FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor, FakeErrorReporter())
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
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, failingDeductor, FakeErrorReporter())
        val vm = createViewModelWithBatchUseCase(batchUseCase = batchUseCase)

        assertIs<ProcessingUiState.Error>(vm.uiState.value)
    }

    @Test
    fun `retry resumes from the first unfinished photo instead of restarting the batch`() {
        // 2 photos: photo1's deduction succeeds and it generates fine. Photo2's
        // deduction fails once (e.g. a transient RC error), which aborts the flow
        // and surfaces the error state. Retry should resume at photo2 only —
        // photo1 must not be re-deducted or re-generated.
        val creditDeductor = FakeCreditDeductor(failDeductAtCall = 2)
        val repo = FakeGenerationRepository()
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
        val vm = createViewModelWithDeductor(
            config = twoPhotoConfig,
            generationRepo = repo,
            creditDeductor = creditDeductor
        )

        assertIs<ProcessingUiState.Error>(vm.uiState.value)
        assertEquals(1, repo.callCountByPhotoId[testPhoto.id])
        assertEquals(null, repo.callCountByPhotoId[testPhoto2.id])

        vm.handleAction(ProcessingUiAction.OnRetryClicked)

        // Total deductions across both attempts: photo1 once, photo2's failed
        // attempt once, photo2's retry once = 3, not 4 (which a full restart
        // would produce: photo1 twice + photo2 twice).
        assertEquals(3, creditDeductor.deductCalled)
        assertEquals(1, repo.callCountByPhotoId[testPhoto.id], "photo1 must not be re-generated")
        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
    }

    @Test
    fun `refunded credits count is preserved across a retry`() {
        // Photo1 generates but fails (refunded). Photo2's deduction then fails,
        // aborting the flow — the refund from photo1 must survive into retry.
        val creditDeductor = FakeCreditDeductor(failDeductAtCall = 2)
        val repo = FakeGenerationRepository(shouldFail = true)
        val resultsHolder = FakeGenerationResultsHolder()
        val twoPhotoConfig = testConfig.copy(photos = listOf(testPhoto, testPhoto2), units = unitsFor(listOf(testPhoto, testPhoto2), listOf(testStyle)))
        val vm = createViewModelWithDeductor(
            config = twoPhotoConfig,
            generationRepo = repo,
            creditDeductor = creditDeductor,
            resultsHolder = resultsHolder
        )

        assertIs<ProcessingUiState.Error>(vm.uiState.value)
        assertEquals(1, creditDeductor.refundCalled)

        // Retry: photo2's deduction now succeeds, but its generation also fails
        // (repo.shouldFail = true), so it gets refunded too. With every unit now
        // failed, the batch surfaces the all-failed error carrying both refunds.
        creditDeductor.stopFailingDeductions()
        vm.handleAction(ProcessingUiAction.OnRetryClicked)

        assertEquals(2, creditDeductor.refundCalled)
        assertEquals(ProcessingUiState.Error.AllFailed(refundedCredits = 2), vm.uiState.value)
    }

    @Test
    fun `insufficient credits exception maps to dedicated error state`() {
        val failingDeductor = FakeCreditDeductor(insufficientCredits = true)
        val errorReporter = FakeErrorReporter()
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, failingDeductor, FakeErrorReporter())
        val vm = createViewModelWithBatchUseCase(batchUseCase = batchUseCase)

        assertIs<ProcessingUiState.Error.InsufficientCredits>(vm.uiState.value)
    }

    @Test
    fun `get credits click navigates to credit store`() {
        val failingDeductor = FakeCreditDeductor(insufficientCredits = true)
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), FakeErrorReporter())
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, failingDeductor, FakeErrorReporter())
        val vm = createViewModelWithBatchUseCase(batchUseCase = batchUseCase)

        vm.handleAction(ProcessingUiAction.OnGetCreditsClicked)

        assertIs<ProcessingNavigationAction.GoToCreditStore>(vm.navigationEvent.value)
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

    @Test
    fun `saved history results are stamped with the config batchId`() {
        val historyRepo = FakeHistoryRepository()
        createViewModel(historyRepo = historyRepo)

        assertEquals(1, historyRepo.savedResults.size)
        assertEquals("test-batch-id", historyRepo.savedResults.first().batchId)
    }

    @Test
    fun `one photo with two styles generates two units and deducts two credits`() {
        val creditDeductor = FakeCreditDeductor()
        val multiStyleConfig = testConfig.copy(units = unitsFor(listOf(testPhoto), listOf(testStyle, testStyle2)))
        val generatePreview = GeneratePreviewUseCase(FakeGenerationRepository(), FakeHistoryRepository(), FakeErrorReporter())
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor, FakeErrorReporter())
        val resultsHolder = FakeGenerationResultsHolder()
        val vm = ProcessingViewModel(
            generationConfigHolder = FakeGenerationConfigHolder(multiStyleConfig),
            generationResultsHolder = resultsHolder,
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = FakeAnalyticsService(),
            completionDelayMs = 0L
        )

        assertIs<ProcessingUiState.Complete>(vm.uiState.value)
        assertEquals(2, resultsHolder.currentResults?.size)
        assertEquals(2, creditDeductor.deductCalled)
        assertTrue(creditDeductor.deductKeys[0].contains("clean_white"))
        assertTrue(creditDeductor.deductKeys[1].contains("warm_linen"))
        assertTrue(creditDeductor.deductKeys.all { it.startsWith("test-batch-id-photo_1-") })
    }

    @Test
    fun `multi-style results carry each style`() {
        val multiStyleConfig = testConfig.copy(units = unitsFor(listOf(testPhoto), listOf(testStyle, testStyle2)))
        val resultsHolder = FakeGenerationResultsHolder()
        createViewModel(config = multiStyleConfig, resultsHolder = resultsHolder)

        val styles = resultsHolder.currentResults
            ?.filterIsInstance<GenerationResult.Success>()
            ?.map { it.style.id }
        assertEquals(listOf("clean_white", "warm_linen"), styles)
    }

    @Test
    fun `mid-batch failure in multi-style run refunds one credit`() {
        val creditDeductor = FakeCreditDeductor()
        val multiStyleConfig = testConfig.copy(units = unitsFor(listOf(testPhoto), listOf(testStyle, testStyle2)))
        val repo = FakeGenerationRepository(failOnIndex = 1)
        val generatePreview = GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter())
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor, FakeErrorReporter())
        val resultsHolder = FakeGenerationResultsHolder()
        ProcessingViewModel(
            generationConfigHolder = FakeGenerationConfigHolder(multiStyleConfig),
            generationResultsHolder = resultsHolder,
            generateBatchPreviewsUseCase = batchUseCase,
            analyticsService = FakeAnalyticsService(),
            completionDelayMs = 0L
        )

        assertEquals(1, creditDeductor.refundCalled)
        assertEquals(1, resultsHolder.refundedCredits)
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
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, FakeCreditDeductor(), FakeErrorReporter())

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
            FakeCreditDeductor(),
            FakeErrorReporter()
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

    private fun createViewModelWithDeductor(
        config: GenerationConfig? = testConfig,
        resultsHolder: GenerationResultsHolder = FakeGenerationResultsHolder(),
        generationRepo: GenerationRepository = FakeGenerationRepository(),
        creditDeductor: CreditDeductor = FakeCreditDeductor(),
        historyRepo: HistoryRepository = FakeHistoryRepository(),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ): ProcessingViewModel {
        val errorReporter = FakeErrorReporter()
        val generatePreview = GeneratePreviewUseCase(generationRepo, historyRepo, errorReporter)
        val batchUseCase = GenerateBatchPreviewsUseCase(generatePreview, creditDeductor, FakeErrorReporter())

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
        private val deductShouldFail: Boolean = false,
        private val insufficientCredits: Boolean = false,
        private val failDeductAtCall: Int = -1
    ) : CreditDeductor {
        var deductCalled = 0
        var refundCalled = 0
        val deductKeys = mutableListOf<String>()
        private var stopFailing = false

        fun stopFailingDeductions() { stopFailing = true }

        override suspend fun deductGenerationCredit(idempotencyKey: String): Result<UserCredits> {
            deductCalled++
            deductKeys.add(idempotencyKey)
            return when {
                insufficientCredits -> Result.failure(InsufficientCreditsException())
                deductShouldFail -> Result.failure(RuntimeException("Insufficient credits"))
                deductCalled == failDeductAtCall && !stopFailing -> Result.failure(RuntimeException("Transient RC error"))
                else -> Result.success(UserCredits(100 - deductCalled))
            }
        }

        override suspend fun refundGenerationCredit(idempotencyKey: String): Result<UserCredits> {
            refundCalled++
            return Result.success(UserCredits(100))
        }
    }

    private open class FakeGenerationRepository(
        private val shouldFail: Boolean = false,
        private val failOnIndex: Int = -1
    ) : GenerationRepository {
        private var counter = 0
        val callCountByPhotoId = mutableMapOf<String, Int>()

        override suspend fun generateImage(
            photo: ProductPhoto, prompt: String, style: Style,
            exportFormat: ExportFormat, quality: GenerationQuality,
            deductionKey: String?,
            onProgress: (suspend (Float) -> Unit)?
        ): Result<GenerationResult.Success> {
            val index = counter++
            callCountByPhotoId[photo.id] = (callCountByPhotoId[photo.id] ?: 0) + 1
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
            photo: ProductPhoto, prompt: String, style: Style,
            exportFormat: ExportFormat, quality: GenerationQuality,
            deductionKey: String?,
            onProgress: (suspend (Float) -> Unit)?
        ): Result<GenerationResult.Success> {
            progressToEmit.forEach { value ->
                reportedProgress.add(value)
                onProgress?.invoke(value)
            }
            return super.generateImage(photo, prompt, style, exportFormat, quality, deductionKey, onProgress)
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
        override suspend fun setGalleryUri(id: String, galleryUri: String) {}
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
        FakeCreditDeductor(),
        FakeErrorReporter()
    ) {
        override fun invoke(
            config: GenerationConfig,
            resumeState: BatchResumeState,
            onUnitProgress: (suspend (unitIndex: Int, progress: Float) -> Unit)?
        ): Flow<BatchProgress> = flow {
            throw exception
        }
    }
}
