package com.middleton.studiosnap.feature.sessiondetail.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.sessiondetail.presentation.action.SessionDetailUiAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.navigation.SessionDetailNavigationAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.ui_state.SessionDetailUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionDetailViewModelTest : BaseViewModelTest() {

    private val testStyle = Style(
        id = "clean_white",
        displayName = UiText.DynamicString("Clean White"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "white bg"
    )
    private val testPhoto = ProductPhoto(id = "p1", localUri = "content://photo1")

    private fun makeResult(id: String, batchId: String = "batch-A") = GenerationResult.Success(
        generationId = id,
        inputPhoto = testPhoto,
        previewUri = "preview_$id.jpg",
        style = testStyle,
        createdAt = 1000L,
        batchId = batchId
    )

    private fun makeSession(
        sessionId: String = "batch-A",
        label: String? = null,
        styleName: String = "Clean White"
    ) = HistorySession(
        batchId = sessionId,
        thumbnailUris = listOf("preview_1.jpg"),
        imageCount = 1,
        sessionLabel = label,
        styleName = styleName,
        createdAt = 1000L
    )

    // region loading

    @Test
    fun `init loads results for the given session`() {
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession()),
            resultsForSession = listOf(makeResult("gen-1"), makeResult("gen-2"))
        )
        val sut = SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)

        val state = sut.uiState.value
        assertIs<SessionDetailUiState.Success>(state)
        assertEquals(2, state.results.size)
    }

    @Test
    fun `init shows error when session not found`() {
        val repo = FakeHistoryRepository(sessions = emptyList(), resultsForSession = emptyList())
        val sut = SessionDetailViewModel(sessionId = "unknown", historyRepository = repo)

        assertIs<SessionDetailUiState.Error>(sut.uiState.value)
    }

    @Test
    fun `display label uses session label when set`() {
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession(label = "My Product Shoot")),
            resultsForSession = listOf(makeResult("gen-1"))
        )
        val sut = SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)

        val state = assertIs<SessionDetailUiState.Success>(sut.uiState.value)
        assertEquals("My Product Shoot", state.displayLabel)
    }

    @Test
    fun `display label falls back to style name when no label set`() {
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession(label = null, styleName = "Clean White")),
            resultsForSession = listOf(makeResult("gen-1"))
        )
        val sut = SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)

        val state = assertIs<SessionDetailUiState.Success>(sut.uiState.value)
        assertEquals("Clean White", state.displayLabel)
    }

    // endregion

    // region navigation

    @Test
    fun `back action navigates back`() {
        val sut = createSut()
        sut.handleAction(SessionDetailUiAction.OnBackClicked)
        assertIs<SessionDetailNavigationAction.GoBack>(sut.navigationEvent.value)
    }

    @Test
    fun `navigation handled clears event`() {
        val sut = createSut()
        sut.handleAction(SessionDetailUiAction.OnBackClicked)
        sut.handleAction(SessionDetailUiAction.OnNavigationHandled)
        assertNull(sut.navigationEvent.value)
    }

    // endregion

    // region delete

    @Test
    fun `delete session clicked shows confirm dialog`() {
        val sut = createSut()
        sut.handleAction(SessionDetailUiAction.OnDeleteSessionClicked("batch-A"))

        val state = assertIs<SessionDetailUiState.Success>(sut.uiState.value)
        assertTrue(state.showDeleteConfirm)
    }

    @Test
    fun `delete dismissed hides confirm dialog`() {
        val sut = createSut()
        sut.handleAction(SessionDetailUiAction.OnDeleteSessionClicked("batch-A"))
        sut.handleAction(SessionDetailUiAction.OnDeleteDismissed)

        val state = assertIs<SessionDetailUiState.Success>(sut.uiState.value)
        assertTrue(!state.showDeleteConfirm)
    }

    @Test
    fun `delete confirmed calls repository and navigates back`() {
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession()),
            resultsForSession = listOf(makeResult("gen-1"))
        )
        val sut = SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)

        sut.handleAction(SessionDetailUiAction.OnDeleteSessionClicked("batch-A"))
        sut.handleAction(SessionDetailUiAction.OnDeleteConfirmed)

        assertTrue(repo.deletedSessionIds.contains("batch-A"))
        assertIs<SessionDetailNavigationAction.GoBack>(sut.navigationEvent.value)
    }

    // endregion

    // region reactive updates

    @Test
    fun `results update reactively when repository emits`() {
        val resultsFlow = MutableStateFlow(listOf(makeResult("gen-1")))
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession()),
            resultsFlow = resultsFlow
        )
        val sut = SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)

        assertEquals(1, assertIs<SessionDetailUiState.Success>(sut.uiState.value).results.size)

        resultsFlow.value = listOf(makeResult("gen-1"), makeResult("gen-2"))
        assertEquals(2, assertIs<SessionDetailUiState.Success>(sut.uiState.value).results.size)
    }

    // endregion

    // region factory

    private fun createSut(): SessionDetailViewModel {
        val repo = FakeHistoryRepository(
            sessions = listOf(makeSession()),
            resultsForSession = listOf(makeResult("gen-1"))
        )
        return SessionDetailViewModel(sessionId = "batch-A", historyRepository = repo)
    }

    // endregion

    // region fakes

    private class FakeHistoryRepository(
        private val sessions: List<HistorySession> = emptyList(),
        private val resultsForSession: List<GenerationResult.Success> = emptyList(),
        private val resultsFlow: MutableStateFlow<List<GenerationResult.Success>>? = null
    ) : HistoryRepository {
        val deletedSessionIds = mutableListOf<String>()

        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getSessions(): Flow<List<HistorySession>> = flowOf(sessions)
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> =
            resultsFlow ?: flowOf(resultsForSession)
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String) = null
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) { deletedSessionIds.add(sessionId) }
    }

    // endregion
}
