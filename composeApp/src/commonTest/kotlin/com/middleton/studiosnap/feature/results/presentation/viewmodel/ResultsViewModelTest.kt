package com.middleton.studiosnap.feature.results.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.results.domain.usecase.DownloadFullResUseCase
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultsViewModelTest : BaseViewModelTest() {

    private val testStyle = Style(
        id = "clean_white", nameKey = "Clean White",
        categories = setOf(StyleCategory.ALL),
        thumbnailResName = "clean_white",
        kontextPrompt = "white bg"
    )
    private val testPhoto = ProductPhoto(id = "photo_1", localUri = "content://photo1", width = 1024, height = 768)

    private val successResult = GenerationResult.Success(
        generationId = "gen_1",
        inputPhoto = testPhoto,
        watermarkedPreviewUri = "preview.jpg",
        style = testStyle,
        createdAt = 1000L
    )

    @Test
    fun `init loads results from holder`() {
        val vm = createViewModel(results = listOf(successResult))
        assertEquals(1, vm.uiState.value.results.size)
        assertEquals(successResult, vm.uiState.value.results.first().result)
    }

    @Test
    fun `null results holder shows empty state`() {
        val vm = createViewModel(results = null)
        assertTrue(vm.uiState.value.results.isEmpty())
    }

    @Test
    fun `observes credit balance`() {
        val creditsFlow = MutableStateFlow(UserCredits(10))
        val vm = createViewModel(creditsFlow = creditsFlow)
        assertEquals(10, vm.uiState.value.creditBalance)

        creditsFlow.value = UserCredits(5)
        assertEquals(5, vm.uiState.value.creditBalance)
    }

    @Test
    fun `download success marks item purchased`() {
        val vm = createViewModel(results = listOf(successResult))

        vm.handleAction(ResultsUiAction.OnDownloadClicked("gen_1"))

        val item = vm.uiState.value.results.first()
        assertTrue(item.isPurchased)
        assertEquals("/path/full.jpg", item.fullResLocalUri)
    }

    @Test
    fun `download success logs analytics`() {
        val analytics = FakeAnalyticsService()
        val vm = createViewModel(results = listOf(successResult), analyticsService = analytics)

        vm.handleAction(ResultsUiAction.OnDownloadClicked("gen_1"))

        assertTrue(analytics.hasEvent(AnalyticsEvents.DOWNLOAD_COMPLETED))
    }

    @Test
    fun `download with insufficient credits shows snackbar`() {
        val vm = createViewModel(
            results = listOf(successResult),
            creditDeductor = FakeCreditDeductor(shouldSucceed = false)
        )

        vm.handleAction(ResultsUiAction.OnDownloadClicked("gen_1"))

        assertEquals("Not enough credits", vm.uiState.value.snackbarMessage)
    }

    @Test
    fun `download failure shows error snackbar`() {
        val vm = createViewModel(
            results = listOf(successResult),
            generationRepo = FakeGenerationRepository(downloadShouldFail = true)
        )

        vm.handleAction(ResultsUiAction.OnDownloadClicked("gen_1"))

        assertEquals("Download failed", vm.uiState.value.snackbarMessage)
    }

    @Test
    fun `share logs analytics event`() {
        val analytics = FakeAnalyticsService()
        val vm = createViewModel(results = listOf(successResult), analyticsService = analytics)

        vm.handleAction(ResultsUiAction.OnShareClicked("gen_1"))

        assertTrue(analytics.hasEvent(AnalyticsEvents.PREVIEW_SHARED))
    }

    @Test
    fun `done navigates to home`() {
        val vm = createViewModel(results = listOf(successResult))
        vm.handleAction(ResultsUiAction.OnDoneClicked)
        assertIs<ResultsNavigationAction.GoToHome>(vm.navigationEvent.value)
    }

    @Test
    fun `back navigates back`() {
        val vm = createViewModel(results = listOf(successResult))
        vm.handleAction(ResultsUiAction.OnBackClicked)
        assertIs<ResultsNavigationAction.GoBack>(vm.navigationEvent.value)
    }

    @Test
    fun `buy credits navigates to credit store`() {
        val vm = createViewModel(results = listOf(successResult))
        vm.handleAction(ResultsUiAction.OnBuyCreditsClicked)
        assertIs<ResultsNavigationAction.GoToCreditStore>(vm.navigationEvent.value)
    }

    @Test
    fun `snackbar dismissed clears message`() {
        val vm = createViewModel(
            results = listOf(successResult),
            creditDeductor = FakeCreditDeductor(shouldSucceed = false)
        )
        vm.handleAction(ResultsUiAction.OnDownloadClicked("gen_1"))
        assertEquals("Not enough credits", vm.uiState.value.snackbarMessage)

        vm.handleAction(ResultsUiAction.OnSnackbarDismissed)
        assertNull(vm.uiState.value.snackbarMessage)
    }

    @Test
    fun `navigation handled clears event`() {
        val vm = createViewModel(results = listOf(successResult))
        vm.handleAction(ResultsUiAction.OnDoneClicked)
        assertIs<ResultsNavigationAction.GoToHome>(vm.navigationEvent.value)

        vm.onNavigationHandled()
        assertNull(vm.navigationEvent.value)
    }

    // --- Factory ---

    private fun createViewModel(
        results: List<GenerationResult>? = listOf(successResult),
        creditsFlow: Flow<UserCredits> = flowOf(UserCredits(10)),
        generationRepo: GenerationRepository = FakeGenerationRepository(),
        creditDeductor: CreditDeductor = FakeCreditDeductor(),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ): ResultsViewModel {
        val resultsHolder = FakeGenerationResultsHolder(results)
        val creditQueries = FakeCreditQueries(creditsFlow)
        val historyRepo = FakeHistoryRepository()
        val downloadUseCase = DownloadFullResUseCase(
            generationRepo, historyRepo, creditDeductor, FakeErrorReporter()
        )
        return ResultsViewModel(
            generationResultsHolder = resultsHolder,
            downloadFullResUseCase = downloadUseCase,
            creditQueries = creditQueries,
            analyticsService = analyticsService
        )
    }

    // --- Fakes ---

    private class FakeGenerationResultsHolder(
        override var currentResults: List<GenerationResult>? = null
    ) : GenerationResultsHolder

    private class FakeCreditQueries(
        private val flow: Flow<UserCredits> = flowOf(UserCredits(10))
    ) : CreditQueries {
        override suspend fun getUserCredits() = Result.success(UserCredits(10))
        override suspend fun refreshCredits() = Result.success(UserCredits(10))
        override fun observeCredits() = flow
    }

    private class FakeGenerationRepository(
        private val downloadShouldFail: Boolean = false
    ) : GenerationRepository {
        override suspend fun generateImage(
            photo: ProductPhoto, style: Style, shadow: Boolean,
            reflection: Boolean, exportFormat: ExportFormat, quality: GenerationQuality
        ) = Result.success(
            GenerationResult.Success(
                generationId = "gen_1", inputPhoto = photo,
                watermarkedPreviewUri = "preview.jpg", style = style, createdAt = 0L
            )
        )
        override suspend fun downloadFullRes(generationId: String): Result<String> {
            return if (downloadShouldFail) Result.failure(RuntimeException("Network error"))
            else Result.success("/path/full.jpg")
        }
    }

    private class FakeCreditDeductor(
        private val shouldSucceed: Boolean = true
    ) : CreditDeductor {
        override suspend fun deductCredits(amount: Int, reason: String) =
            if (shouldSucceed) Result.success(UserCredits(9)) else Result.failure(Exception("No credits"))
        override suspend fun refundCredits(amount: Int, reason: String) =
            Result.success(UserCredits(10))
    }

    private class FakeHistoryRepository : HistoryRepository {
        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getPurchasedOnly(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getPreviewsOnly(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String) = null
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
    }

    private class FakeErrorReporter : ErrorReporter {
        override fun recordException(exception: Throwable) {}
    }
}
