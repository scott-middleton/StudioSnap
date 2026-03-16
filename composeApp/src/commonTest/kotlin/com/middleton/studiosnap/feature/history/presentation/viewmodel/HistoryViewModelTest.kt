package com.middleton.studiosnap.feature.history.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.service.FakeAnalyticsService
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.action.HistoryUiAction
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryFilter
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

    private fun makeResult(
        id: String,
        purchased: Boolean = false
    ) = GenerationResult.Success(
        generationId = id,
        inputPhoto = testPhoto,
        previewUri = "preview_$id.jpg",
        fullResUri = if (purchased) "/path/full_$id.jpg" else null,
        style = testStyle,
        createdAt = 1000L,
        imageWidth = 512,
        imageHeight = 512
    )

    @Test
    fun `init loads all items from repository`() {
        val results = listOf(makeResult("gen_1"), makeResult("gen_2"))
        val vm = createViewModel(results)

        assertEquals(2, vm.uiState.value.items.size)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `empty history shows empty state`() {
        val vm = createViewModel(emptyList())

        assertTrue(vm.uiState.value.isEmpty)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `filter purchased shows only purchased items`() {
        val results = listOf(
            makeResult("gen_1", purchased = true),
            makeResult("gen_2", purchased = false)
        )
        val vm = createViewModel(results)

        vm.handleAction(HistoryUiAction.OnFilterChanged(HistoryFilter.PURCHASED))

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("gen_1", vm.uiState.value.items.first().id)
        assertEquals(HistoryFilter.PURCHASED, vm.uiState.value.filter)
    }

    @Test
    fun `filter previews shows only unpurchased items`() {
        val results = listOf(
            makeResult("gen_1", purchased = true),
            makeResult("gen_2", purchased = false)
        )
        val vm = createViewModel(results)

        vm.handleAction(HistoryUiAction.OnFilterChanged(HistoryFilter.PREVIEWS))

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("gen_2", vm.uiState.value.items.first().id)
    }

    @Test
    fun `filter all shows everything`() {
        val results = listOf(
            makeResult("gen_1", purchased = true),
            makeResult("gen_2", purchased = false)
        )
        val vm = createViewModel(results)

        vm.handleAction(HistoryUiAction.OnFilterChanged(HistoryFilter.PURCHASED))
        assertEquals(1, vm.uiState.value.items.size)

        vm.handleAction(HistoryUiAction.OnFilterChanged(HistoryFilter.ALL))
        assertEquals(2, vm.uiState.value.items.size)
    }

    @Test
    fun `delete removes item from repository`() {
        val repo = FakeHistoryRepository()
        repo.items.value = listOf(makeResult("gen_1"), makeResult("gen_2"))
        val vm = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        vm.handleAction(HistoryUiAction.OnDeleteClicked("gen_1"))

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("gen_2", vm.uiState.value.items.first().id)
    }

    @Test
    fun `item click navigates to result detail`() {
        val vm = createViewModel(listOf(makeResult("gen_1")))

        vm.handleAction(HistoryUiAction.OnItemClicked("gen_1"))

        val event = vm.navigationEvent.value
        assertIs<HistoryNavigationAction.GoToResultDetail>(event)
        assertEquals("gen_1", event.resultId)
    }

    @Test
    fun `back navigates back`() {
        val vm = createViewModel(emptyList())
        vm.handleAction(HistoryUiAction.OnBackClicked)
        assertIs<HistoryNavigationAction.GoBack>(vm.navigationEvent.value)
    }

    @Test
    fun `navigation handled clears event`() {
        val vm = createViewModel(emptyList())
        vm.handleAction(HistoryUiAction.OnBackClicked)
        assertIs<HistoryNavigationAction.GoBack>(vm.navigationEvent.value)

        vm.handleAction(HistoryUiAction.OnNavigationHandled)
        assertNull(vm.navigationEvent.value)
    }

    @Test
    fun `reactive updates when repository emits new data`() {
        val repo = FakeHistoryRepository()
        repo.items.value = listOf(makeResult("gen_1"))
        val vm = HistoryViewModel(historyRepository = repo, analyticsService = FakeAnalyticsService())

        assertEquals(1, vm.uiState.value.items.size)

        // Repository emits updated data
        repo.items.value = listOf(makeResult("gen_1"), makeResult("gen_2"))
        assertEquals(2, vm.uiState.value.items.size)
    }

    // --- Factory ---

    private fun createViewModel(
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
        override fun getPurchasedOnly(): Flow<List<GenerationResult.Success>> =
            MutableStateFlow(items.value.filter { it.fullResUri != null })
        override fun getPreviewsOnly(): Flow<List<GenerationResult.Success>> =
            MutableStateFlow(items.value.filter { it.fullResUri == null })
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
    }
}
