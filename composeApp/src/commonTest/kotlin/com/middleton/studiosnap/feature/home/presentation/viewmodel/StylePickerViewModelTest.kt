package com.middleton.studiosnap.feature.home.presentation.viewmodel

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import com.middleton.studiosnap.core.presentation.BaseViewModelTest
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.presentation.action.StylePickerUiAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

class StylePickerViewModelTest : BaseViewModelTest() {

    private val testStyles = listOf(
        Style(
            id = "clean_white",
            displayName = UiText.DynamicString("Clean White"),
            categories = setOf(StyleCategory.ALL),
            thumbnail = null,
            kontextPrompt = "White bg"
        ),
        Style(
            id = "warm_linen",
            displayName = UiText.DynamicString("Warm Linen"),
            categories = setOf(StyleCategory.CLOTHING, StyleCategory.JEWELLERY),
            thumbnail = null,
            kontextPrompt = "Linen bg"
        ),
        Style(
            id = "morning_kitchen",
            displayName = UiText.DynamicString("Morning Kitchen"),
            categories = setOf(StyleCategory.FOOD, StyleCategory.HOMEWARE),
            thumbnail = null,
            kontextPrompt = "Kitchen bg"
        )
    )

    private fun createViewModel(
        styles: List<Style> = testStyles,
        lastCategory: String = "ALL"
    ): StylePickerViewModel {
        return StylePickerViewModel(
            styleRepository = FakeStyleRepository(styles),
            userPreferencesRepository = FakeUserPreferencesRepository(lastCategory = lastCategory)
        )
    }

    @Test
    fun `initial state restores last saved category`() {
        val viewModel = createViewModel(lastCategory = "FOOD")
        val state = viewModel.uiState.value
        assertEquals(StyleCategory.FOOD, state.selectedCategory)
    }

    @Test
    fun `initial state with unknown category defaults to ALL`() {
        val viewModel = createViewModel(lastCategory = "NONEXISTENT")
        val state = viewModel.uiState.value
        assertEquals(StyleCategory.ALL, state.selectedCategory)
        assertEquals(3, state.styles.size)
    }

    @Test
    fun `initial state loads all styles when category is ALL`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(3, state.styles.size)
        assertEquals(StyleCategory.ALL, state.selectedCategory)
    }

    @Test
    fun `initial state filters styles for saved category`() {
        val viewModel = createViewModel(lastCategory = "FOOD")
        val state = viewModel.uiState.value
        assertEquals(1, state.styles.size)
        assertEquals("morning_kitchen", state.styles.first().id)
    }

    @Test
    fun `selecting category filters styles correctly`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnCategorySelected(StyleCategory.CLOTHING))
        val state = viewModel.uiState.value
        assertEquals(StyleCategory.CLOTHING, state.selectedCategory)
        assertEquals(1, state.styles.size)
        assertEquals("warm_linen", state.styles.first().id)
    }

    @Test
    fun `selecting ALL shows all styles`() {
        val viewModel = createViewModel(lastCategory = "FOOD")
        viewModel.handleAction(StylePickerUiAction.OnCategorySelected(StyleCategory.ALL))
        val state = viewModel.uiState.value
        assertEquals(StyleCategory.ALL, state.selectedCategory)
        assertEquals(3, state.styles.size)
    }

    @Test
    fun `selecting category persists to preferences`() {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = StylePickerViewModel(
            styleRepository = FakeStyleRepository(testStyles),
            userPreferencesRepository = prefs
        )
        viewModel.handleAction(StylePickerUiAction.OnCategorySelected(StyleCategory.JEWELLERY))
        assertEquals("JEWELLERY", prefs.savedCategory)
    }

    @Test
    fun `selecting category updates selectedCategory in state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnCategorySelected(StyleCategory.HOMEWARE))
        assertEquals(StyleCategory.HOMEWARE, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `previewing a style sets previewedStyleId without touching other state`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnStylePreviewed("warm_linen"))
        val state = viewModel.uiState.value
        assertEquals("warm_linen", state.previewedStyleId)
        assertEquals(StyleCategory.ALL, state.selectedCategory)
        assertEquals(3, state.styles.size)
    }

    @Test
    fun `heroStyleId returns previewedStyleId when a preview is active`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnInitialise("clean_white"))
        viewModel.handleAction(StylePickerUiAction.OnStylePreviewed("warm_linen"))
        assertEquals("warm_linen", viewModel.uiState.value.heroStyleId)
    }

    @Test
    fun `heroStyleId falls back to confirmedStyleId when no preview exists`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnInitialise("clean_white"))
        assertEquals("clean_white", viewModel.uiState.value.heroStyleId)
    }

    @Test
    fun `onInitialise seeds confirmedStyleId`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnInitialise("morning_kitchen"))
        assertEquals("morning_kitchen", viewModel.uiState.value.confirmedStyleId)
    }

    @Test
    fun `switching category preserves an active preview`() {
        val viewModel = createViewModel()
        viewModel.handleAction(StylePickerUiAction.OnStylePreviewed("morning_kitchen"))
        viewModel.handleAction(StylePickerUiAction.OnCategorySelected(StyleCategory.CLOTHING))
        assertEquals("morning_kitchen", viewModel.uiState.value.previewedStyleId)
    }

    // --- Fakes ---

    private class FakeStyleRepository(private val styles: List<Style>) : StyleRepository {
        override fun getAllStyles(): List<Style> = styles
        override fun getStylesByCategory(category: StyleCategory): List<Style> {
            if (category == StyleCategory.ALL) return styles
            return styles.filter { category in it.categories }
        }
        override fun getStyleById(id: String): Style? = styles.find { it.id == id }
    }

    private class FakeUserPreferencesRepository(
        private val lastCategory: String = "ALL"
    ) : UserPreferencesRepository {
        var savedCategory: String? = null
            private set

        override suspend fun hasCompletedOnboarding() = true
        override suspend fun setHasCompletedOnboarding() {}
        override suspend fun hasPurchasedCredits() = false
        override suspend fun setHasPurchasedCredits() {}
        override fun observeHasUsedFreeGeneration(): Flow<Boolean> = flowOf(false)
        override suspend fun hasUsedFreeGeneration() = false
        override suspend fun setHasUsedFreeGeneration() {}
        override suspend fun getFreeDownloadsUsed() = 0
        override suspend fun incrementFreeDownloads() {}
        override suspend fun incrementAndGetPaidDownloads() = 0
        override suspend fun getLastUsedCategoryFilter() = lastCategory
        override suspend fun setLastUsedCategoryFilter(category: String) {
            savedCategory = category
        }
        override fun observePreferences(): Flow<UserPreferencesSnapshot> = flowOf(
            UserPreferencesSnapshot(
                hasCompletedOnboarding = true,
                hasPurchasedCredits = false,
                hasUsedFreeGeneration = false,
                freeDownloadsUsed = 0,
                totalPaidDownloads = 0,
                lastUsedCategoryFilter = lastCategory
            )
        )
    }
}
