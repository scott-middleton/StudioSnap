package com.middleton.studiosnap.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.middleton.studiosnap.core.presentation.theme.StudioSnapTheme
import com.middleton.studiosnap.feature.history.presentation.HistoryScreenContent
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryFilter
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryItem
import com.middleton.studiosnap.feature.history.presentation.ui_state.HistoryUiState
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.presentation.StylePickerScreenContent
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingHeroPage
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingBeforeAfterPage
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingStyleShowcasePage
import com.middleton.studiosnap.feature.onboarding.presentation.OnboardingValuePage
import com.middleton.studiosnap.feature.processing.presentation.ProcessingScreenContent
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingUiState
import com.middleton.studiosnap.feature.results.presentation.ResultsScreenContent
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultItem
import com.middleton.studiosnap.feature.results.presentation.ui_state.ResultsUiState
import com.middleton.studiosnap.feature.settings.presentation.SettingsScreenContent
import com.middleton.studiosnap.feature.settings.presentation.ui_state.SettingsUiState
import com.middleton.studiosnap.feature.splash.presentation.SplashScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AllScreensSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.Light.NoActionBar"
    )

    @Before
    fun setup() {
        val clazz = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider")
        val field = clazz.getDeclaredField("ANDROID_CONTEXT")
        field.isAccessible = true
        field.set(null, paparazzi.context)
    }

    private fun snapshot(
        darkTheme: Boolean = false,
        content: @Composable () -> Unit
    ) {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                StudioSnapTheme(darkTheme = darkTheme) {
                    content()
                }
            }
        }
    }

    // region — Onboarding

    @Test
    fun onboarding_heroPage() {
        snapshot(darkTheme = true) {
            OnboardingHeroPage(onNext = {})
        }
    }

    @Test
    fun onboarding_beforeAfterPage() {
        snapshot(darkTheme = true) {
            OnboardingBeforeAfterPage(isPageSettled = false, onNext = {})
        }
    }

    @Test
    fun onboarding_styleShowcasePage() {
        snapshot(darkTheme = true) {
            OnboardingStyleShowcasePage(onNext = {})
        }
    }

    @Test
    fun onboarding_valuePage() {
        snapshot(darkTheme = true) {
            OnboardingValuePage(onGetStarted = {})
        }
    }

    // endregion

    // region — Processing

    @Test
    fun processing_loading() {
        snapshot {
            ProcessingScreenContent(
                state = ProcessingUiState.Loading,
                onAction = {}
            )
        }
    }

    @Test
    fun processing_inProgress() {
        snapshot {
            ProcessingScreenContent(
                state = ProcessingUiState.Processing(
                    currentPhotoIndex = 1,
                    totalPhotos = 3,
                    styleName = "Rustic Wood",
                    overallProgress = 0.45f
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun processing_error() {
        snapshot {
            ProcessingScreenContent(
                state = ProcessingUiState.Error("Network connection lost. Please check your connection and try again."),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Results

    @Test
    fun results_withResults() {
        snapshot {
            ResultsScreenContent(
                state = ResultsUiState(
                    results = listOf(
                        ResultItem(
                            result = GenerationResult.Success(
                                generationId = "gen_1",
                                inputPhoto = ProductPhoto(id = "p1", localUri = "file:///fake/photo.jpg"),
                                previewUri = "file:///fake/preview.jpg",
                                style = fakeStyle(),
                                createdAt = System.currentTimeMillis(),
                                imageWidth = 1024,
                                imageHeight = 1024
                            ),
                            isPurchased = false
                        )
                    ),
                    creditBalance = 10
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun results_empty() {
        snapshot {
            ResultsScreenContent(
                state = ResultsUiState(),
                onAction = {}
            )
        }
    }

    // endregion

    // region — History

    @Test
    fun history_withItems() {
        snapshot {
            HistoryScreenContent(
                state = HistoryUiState(
                    items = listOf(
                        HistoryItem(
                            id = "h1",
                            inputPhotoUri = "file:///fake/input.jpg",
                            previewUri = "file:///fake/preview1.jpg",
                            fullResLocalUri = null,
                            styleName = "Rustic Wood",
                            isPurchased = true,
                            createdAt = System.currentTimeMillis(),
                            imageWidth = 1024,
                            imageHeight = 1024
                        ),
                        HistoryItem(
                            id = "h2",
                            inputPhotoUri = "file:///fake/input2.jpg",
                            previewUri = "file:///fake/preview2.jpg",
                            fullResLocalUri = null,
                            styleName = "Marble Luxe",
                            isPurchased = false,
                            createdAt = System.currentTimeMillis() - 86400000,
                            imageWidth = 1024,
                            imageHeight = 1024
                        )
                    ),
                    filter = HistoryFilter.ALL,
                    isLoading = false
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun history_empty() {
        snapshot {
            HistoryScreenContent(
                state = HistoryUiState(isLoading = false),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Style Picker

    @Test
    fun stylePicker_default() {
        snapshot {
            StylePickerScreenContent(
                styles = fakeStyles(),
                selectedStyleId = null,
                selectedCategory = StyleCategory.ALL,
                onCategorySelected = {},
                onStyleSelected = {},
                onClose = {}
            )
        }
    }

    @Test
    fun stylePicker_withSelection() {
        snapshot {
            StylePickerScreenContent(
                styles = fakeStyles(),
                selectedStyleId = "rustic_wood",
                selectedCategory = StyleCategory.ALL,
                onCategorySelected = {},
                onStyleSelected = {},
                onClose = {}
            )
        }
    }

    // endregion

    // region — Settings

    @Test
    fun settings_default() {
        snapshot {
            SettingsScreenContent(
                state = SettingsUiState(
                    creditBalance = 25,
                    isSignedIn = true,
                    appVersion = "1.0.0"
                ),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Helpers

    private fun fakeStyle() = Style(
        id = "rustic_wood",
        nameKey = "rustic_wood",
        categories = emptySet(),
        thumbnailResName = "swatch_wood",
        kontextPrompt = "Change only the background to rustic wood."
    )

    private fun fakeStyles(): List<Style> = listOf(
        Style("clean_white", "clean_white", setOf(StyleCategory.ALL), "", "Clean white background"),
        Style("warm_linen", "warm_linen", setOf(StyleCategory.ALL), "", "Warm linen background"),
        Style("marble_luxe", "marble_luxe", setOf(StyleCategory.ALL), "", "Marble background"),
        Style("rustic_wood", "rustic_wood", setOf(StyleCategory.ALL), "swatch_wood", "Rustic wood background"),
        Style("dark_moody", "dark_moody", setOf(StyleCategory.ALL), "swatch_dark", "Dark moody background"),
        Style("botanical_garden", "botanical_garden", setOf(StyleCategory.ALL), "swatch_botanical", "Botanical garden background")
    )

    // endregion
}
