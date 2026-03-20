package com.middleton.studiosnap.feature.history.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryViewModelTest : BaseViewModelTest() {

    private val testStyle = Style(
        id = "clean_white", displayName = UiText.DynamicString("Clean White"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "white bg"
    )
    private val testPhoto = ProductPhoto(id = "p1", localUri = "content://photo1", width = 1024, height = 768)

    private fun makeResult(id: String) = GenerationResult.Success(
        generationId = id,
        inputPhoto = testPhoto,
        previewUri = "preview_$id.jpg",
        style = testStyle,
        createdAt = 1000L,
        imageWidth = 512,
        imageHeight = 512
    )

    @Test
    fun `init loads all items from repository`() {
        val sut = createSut(listOf(makeResult("gen_1"), makeResult("gen_2")))

        assertEquals(2, sut.uiState.value.items.size)
        assertFalse(sut.uiState.value.isLoading)
    }

    @Test
    fun `empty history shows empty state`() {
        val sut = createSut(emptyList())

        assertTrue(sut.uiState.value.isEmpty)
        assertTrue(sut.uiState.value.items.isEmpty())
    }

    @Test
    fun `delete removes item from repository`() {
        val repo = FakeHistoryRepository()
        repo.items.value = listOf(makeResult("gen_1"), makeResult("gen_2"))
        val sut = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        sut.handleAction(HistoryUiAction.OnDeleteClicked("gen_1"))

        assertEquals(1, sut.uiState.value.items.size)
        assertEquals("gen_2", sut.uiState.value.items.first().id)
    }

    @Test
    fun `item click navigates to result detail`() {
        val sut = createSut(listOf(makeResult("gen_1")))

        sut.handleAction(HistoryUiAction.OnItemClicked("gen_1"))

        val event = sut.navigationEvent.value
        assertIs<HistoryNavigationAction.GoToResultDetail>(event)
        assertEquals("gen_1", event.resultId)
    }

    @Test
    fun `back navigates back`() {
        val sut = createSut(emptyList())
        sut.handleAction(HistoryUiAction.OnBackClicked)
        assertIs<HistoryNavigationAction.GoBack>(sut.navigationEvent.value)
    }

    @Test
    fun `navigation handled clears event`() {
        val sut = createSut(emptyList())
        sut.handleAction(HistoryUiAction.OnBackClicked)
        assertIs<HistoryNavigationAction.GoBack>(sut.navigationEvent.value)

        sut.handleAction(HistoryUiAction.OnNavigationHandled)
        assertNull(sut.navigationEvent.value)
    }

    @Test
    fun `reactive updates when repository emits new data`() {
        val repo = FakeHistoryRepository()
        repo.items.value = listOf(makeResult("gen_1"))
        val sut = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        assertEquals(1, sut.uiState.value.items.size)

        repo.items.value = listOf(makeResult("gen_1"), makeResult("gen_2"))
        assertEquals(2, sut.uiState.value.items.size)
    }

    @Test
    fun `items map fields correctly`() {
        val sut = createSut(listOf(makeResult("gen_1")))

        val item = sut.uiState.value.items.first()
        assertEquals("gen_1", item.id)
        assertEquals("preview_gen_1.jpg", item.previewUri)
        assertEquals(1000L, item.createdAt)
        assertEquals("Clean White", item.styleName)
    }

    // --- Factory ---

    private fun createSut(
        results: List<GenerationResult.Success>
    ): HistoryViewModel {
        val repo = FakeHistoryRepository()
        repo.items.value = results
        return HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())
    }

    // --- Fakes ---

    private class FakeHistoryRepository : HistoryRepository {
        val items = MutableStateFlow<List<GenerationResult.Success>>(emptyList())

        override fun getAll(): Flow<List<GenerationResult.Success>> = items
        override fun getSessions() = kotlinx.coroutines.flow.flowOf(emptyList<com.middleton.studiosnap.feature.history.domain.model.HistorySession>())
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> = kotlinx.coroutines.flow.flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {
            items.value = items.value + result
        }
        override suspend fun saveAll(results: List<GenerationResult.Success>) {
            items.value = items.value + results
        }
        override suspend fun getById(id: String) = items.value.find { it.generationId == id }
        override suspend fun delete(id: String) {
            items.value = items.value.filter { it.generationId != id }
        }
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }
}
