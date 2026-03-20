package com.middleton.studiosnap.feature.history.presentation.viewmodel

import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryViewModelTest : BaseViewModelTest() {

    private fun makeSession(id: String, createdAt: Long = 1000L) = HistorySession(
        batchId = id,
        thumbnailUris = listOf("preview_$id.jpg"),
        imageCount = 1,
        sessionLabel = null,
        styleName = "Clean White",
        createdAt = createdAt
    )

    @Test
    fun `init loads sessions from repository`() {
        val sut = createSut(listOf(makeSession("s1"), makeSession("s2")))

        assertEquals(2, sut.uiState.value.sessions.size)
        assertFalse(sut.uiState.value.isLoading)
    }

    @Test
    fun `empty sessions shows empty state`() {
        val sut = createSut(emptyList())

        assertTrue(sut.uiState.value.isEmpty)
        assertTrue(sut.uiState.value.sessions.isEmpty())
    }

    @Test
    fun `session click navigates to session detail`() {
        val sut = createSut(listOf(makeSession("s1")))

        sut.handleAction(HistoryUiAction.OnSessionClicked("s1"))

        val event = sut.navigationEvent.value
        assertIs<HistoryNavigationAction.GoToSessionDetail>(event)
        assertEquals("s1", event.sessionId)
    }

    @Test
    fun `delete session calls repository`() {
        val repo = FakeHistoryRepository()
        repo.sessions.value = listOf(makeSession("s1"), makeSession("s2"))
        val sut = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        sut.handleAction(HistoryUiAction.OnDeleteSessionClicked("s1"))

        assertTrue(repo.deletedSessionIds.contains("s1"))
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
        sut.handleAction(HistoryUiAction.OnNavigationHandled)
        assertNull(sut.navigationEvent.value)
    }

    @Test
    fun `reactive updates when repository emits new sessions`() {
        val repo = FakeHistoryRepository()
        repo.sessions.value = listOf(makeSession("s1"))
        val sut = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        assertEquals(1, sut.uiState.value.sessions.size)

        repo.sessions.value = listOf(makeSession("s1"), makeSession("s2"))
        assertEquals(2, sut.uiState.value.sessions.size)
    }

    // --- Factory ---

    private fun createSut(sessions: List<HistorySession>): HistoryViewModel {
        val repo = FakeHistoryRepository()
        repo.sessions.value = sessions
        return HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())
    }

    // --- Fakes ---

    private class FakeHistoryRepository : HistoryRepository {
        val sessions = MutableStateFlow<List<HistorySession>>(emptyList())
        val deletedSessionIds = mutableListOf<String>()

        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getSessions(): Flow<List<HistorySession>> = sessions
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String) = null
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) { deletedSessionIds.add(sessionId) }
    }
}
