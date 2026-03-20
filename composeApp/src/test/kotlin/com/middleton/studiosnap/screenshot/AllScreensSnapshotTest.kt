package com.middleton.studiosnap.screenshot

import com.middleton.studiosnap.core.domain.model.UiText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.StudioSnapTheme
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.presentation.HistoryScreenContent
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
import com.middleton.studiosnap.feature.processing.presentation.ui_state.ProcessingStatus
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

    private fun onboardingSnapshot(
        content: @Composable () -> Unit
    ) {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                StudioSnapTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.SplashLightGreen,
                                        AppColors.SplashMidGreen,
                                        AppColors.SplashDarkGreen
                                    ),
                                    radius = 900f
                                )
                            )
                    ) {
                        content()
                    }
                }
            }
        }
    }

    // region — Onboarding

    @Test
    fun onboarding_heroPage() {
        onboardingSnapshot {
            OnboardingHeroPage(onNext = {})
        }
    }

    @Test
    fun onboarding_beforeAfterPage() {
        onboardingSnapshot {
            OnboardingBeforeAfterPage(isPageSettled = false, onNext = {})
        }
    }

    @Test
    fun onboarding_styleShowcasePage() {
        onboardingSnapshot {
            OnboardingStyleShowcasePage(onNext = {})
        }
    }

    @Test
    fun onboarding_valuePage() {
        onboardingSnapshot {
            OnboardingValuePage(animationTrigger = 1, onGetStarted = {})
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
                    styleName = UiText.DynamicString("Rustic Wood"),
                    status = ProcessingStatus.Generating
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
                            )
                        )
                    )
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun results_showingOriginal() {
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
                            showingOriginal = true
                        )
                    )
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun results_saved() {
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
                            isSavedToGallery = true
                        )
                    )
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
                    sessions = listOf(
                        HistorySession(
                            batchId = "s1",
                            thumbnailUris = listOf("file:///fake/preview1.jpg"),
                            imageCount = 3,
                            sessionLabel = null,
                            styleName = "Rustic Wood",
                            createdAt = 1710000000000L
                        ),
                        HistorySession(
                            batchId = "s2",
                            thumbnailUris = listOf("file:///fake/preview2.jpg"),
                            imageCount = 1,
                            sessionLabel = "My custom label",
                            styleName = "Marble Luxe",
                            createdAt = 1709913600000L
                        )
                    ),
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
        displayName = UiText.DynamicString("rustic_wood"),
        categories = emptySet(),
        thumbnail = null,
        kontextPrompt = "Change only the background to rustic wood."
    )

    private fun fakeStyles(): List<Style> = listOf(
        Style("clean_white", UiText.DynamicString("Clean White"), setOf(StyleCategory.ALL), null, "Clean white background"),
        Style("warm_linen", UiText.DynamicString("Warm Linen"), setOf(StyleCategory.ALL), null, "Warm linen background"),
        Style("marble_luxe", UiText.DynamicString("Marble Luxe"), setOf(StyleCategory.ALL), null, "Marble background"),
        Style("rustic_wood", UiText.DynamicString("Rustic Wood"), setOf(StyleCategory.ALL), null, "Rustic wood background"),
        Style("dark_moody", UiText.DynamicString("Dark Moody"), setOf(StyleCategory.ALL), null, "Dark moody background"),
        Style("botanical_garden", UiText.DynamicString("Botanical Garden"), setOf(StyleCategory.ALL), null, "Botanical garden background")
    )

    // endregion
}
