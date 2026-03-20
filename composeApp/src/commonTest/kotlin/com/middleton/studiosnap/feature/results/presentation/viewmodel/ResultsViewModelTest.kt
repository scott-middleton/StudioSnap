package com.middleton.studiosnap.feature.results.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.results.domain.usecase.SaveToGalleryUseCase
import com.middleton.studiosnap.feature.results.presentation.action.ResultsUiAction
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultsViewModelTest : BaseViewModelTest() {

    private val testStyle = Style(
        id = "clean_white",
        displayName = UiText.DynamicString("Clean White"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "white bg"
    )
    private val testPhoto = ProductPhoto(id = "photo_1", localUri = "content://photo1", width = 1024, height = 768)

    private val successResult = GenerationResult.Success(
        generationId = "gen_1",
        inputPhoto = testPhoto,
        previewUri = "/data/preview.jpg",
        style = testStyle,
        createdAt = 1000L
    )

    @Test
    fun `init loads results from holder`() {
        val sut = createSut(results = listOf(successResult))
        assertEquals(1, sut.uiState.value.results.size)
        assertEquals(successResult, sut.uiState.value.results.first().result)
    }

    @Test
    fun `null results holder shows empty state`() {
        val sut = createSut(results = null)
        assertTrue(sut.uiState.value.results.isEmpty())
    }

    @Test
    fun `auto save on init marks all items as saved to gallery`() {
        val sut = createSut(results = listOf(successResult))
        assertTrue(sut.uiState.value.results.first().isSavedToGallery)
    }

    @Test
    fun `auto save on init marks multiple items as saved to gallery`() {
        val result2 = successResult.copy(generationId = "gen_2")
        val sut = createSut(results = listOf(successResult, result2))
        assertTrue(sut.uiState.value.results.all { it.isSavedToGallery })
    }

    @Test
    fun `auto save failure does not mark item as saved to gallery`() {
        val sut = createSut(
            results = listOf(successResult),
            galleryRepo = FakeGalleryRepository(shouldFail = true)
        )
        assertFalse(sut.uiState.value.results.first().isSavedToGallery)
    }

    @Test
    fun `auto save failure shows error snackbar`() {
        val sut = createSut(
            results = listOf(successResult),
            galleryRepo = FakeGalleryRepository(shouldFail = true)
        )
        assertIs<UiText.StringResource>(sut.uiState.value.snackbarMessage)
    }

    @Test
    fun `auto save failure logs analytics`() {
        val analytics = FakeAnalyticsService()
        val sut = createSut(
            results = listOf(successResult),
            galleryRepo = FakeGalleryRepository(shouldFail = true),
            analyticsService = analytics
        )
        assertTrue(analytics.hasEvent(AnalyticsEvents.DOWNLOAD_FAILED))
    }

    @Test
    fun `auto save success logs analytics`() {
        val analytics = FakeAnalyticsService()
        val sut = createSut(results = listOf(successResult), analyticsService = analytics)
        assertTrue(analytics.hasEvent(AnalyticsEvents.DOWNLOAD_COMPLETED))
    }

    @Test
    fun `isAutoSaving is false after save completes`() {
        val sut = createSut(results = listOf(successResult))
        assertFalse(sut.uiState.value.isAutoSaving)
    }

    @Test
    fun `toggle before after flips showingOriginal`() {
        val sut = createSut(results = listOf(successResult))
        assertFalse(sut.uiState.value.results.first().showingOriginal)

        sut.handleAction(ResultsUiAction.OnToggleBeforeAfter("gen_1"))
        assertTrue(sut.uiState.value.results.first().showingOriginal)

        sut.handleAction(ResultsUiAction.OnToggleBeforeAfter("gen_1"))
        assertFalse(sut.uiState.value.results.first().showingOriginal)
    }

    // Share analytics are logged only on success of the platform shareImage() call.
    // That call requires a real Android context / iOS view controller and cannot be
    // meaningfully unit-tested here — covered by manual/integration testing.

    @Test
    fun `done navigates to home`() {
        val sut = createSut(results = listOf(successResult))
        sut.handleAction(ResultsUiAction.OnDoneClicked)
        assertIs<ResultsNavigationAction.GoToHome>(sut.navigationEvent.value)
    }

    @Test
    fun `back navigates back`() {
        val sut = createSut(results = listOf(successResult))
        sut.handleAction(ResultsUiAction.OnBackClicked)
        assertIs<ResultsNavigationAction.GoBack>(sut.navigationEvent.value)
    }

    @Test
    fun `snackbar dismissed clears message`() {
        val sut = createSut(
            results = listOf(successResult),
            galleryRepo = FakeGalleryRepository(shouldFail = true)
        )
        assertIs<UiText.StringResource>(sut.uiState.value.snackbarMessage)

        sut.handleAction(ResultsUiAction.OnSnackbarDismissed)
        assertNull(sut.uiState.value.snackbarMessage)
    }

    @Test
    fun `navigation handled clears event`() {
        val sut = createSut(results = listOf(successResult))
        sut.handleAction(ResultsUiAction.OnDoneClicked)
        assertIs<ResultsNavigationAction.GoToHome>(sut.navigationEvent.value)

        sut.handleAction(ResultsUiAction.OnNavigationHandled)
        assertNull(sut.navigationEvent.value)
    }

    // --- Factory ---

    private fun createSut(
        results: List<GenerationResult>? = listOf(successResult),
        galleryRepo: GalleryRepository = FakeGalleryRepository(),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ): ResultsViewModel {
        val resultsHolder = FakeGenerationResultsHolder(results)
        val saveToGalleryUseCase = SaveToGalleryUseCase(galleryRepo, FakeErrorReporter())
        return ResultsViewModel(
            generationResultsHolder = resultsHolder,
            saveToGalleryUseCase = saveToGalleryUseCase,
            analyticsService = analyticsService
        )
    }

    // --- Fakes ---

    private class FakeGenerationResultsHolder(
        override var currentResults: List<GenerationResult>? = null,
        override var refundedCredits: Int = 0
    ) : GenerationResultsHolder

    private class FakeGalleryRepository(
        private val shouldFail: Boolean = false
    ) : GalleryRepository {
        override suspend fun saveImage(filePath: String, displayName: String): Result<String> {
            return if (shouldFail) Result.failure(RuntimeException("Save error"))
            else Result.success("content://gallery/$displayName")
        }

        override suspend fun deleteImage(galleryUri: String) = Result.success(Unit)
        override suspend fun imageExists(galleryUri: String) = true
    }

    private class FakeErrorReporter : ErrorReporter {
        override fun recordException(exception: Throwable) {}
    }
}
