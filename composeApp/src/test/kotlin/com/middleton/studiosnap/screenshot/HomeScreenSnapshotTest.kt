package com.middleton.studiosnap.screenshot

import com.middleton.studiosnap.core.domain.model.UiText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.middleton.studiosnap.core.presentation.theme.StudioSnapTheme
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.presentation.HomeScreenContent
import com.middleton.studiosnap.feature.home.presentation.ui_state.HomeUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.Light.NoActionBar"
    )

    @Before
    fun setup() {
        // Compose Multiplatform resources need Android context.
        // AndroidContextProvider is a ContentProvider that sets a static field.
        // In Paparazzi tests, no ContentProvider runs, so we set it via reflection.
        val clazz = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider")
        val field = clazz.getDeclaredField("ANDROID_CONTEXT")
        field.isAccessible = true
        field.set(null, paparazzi.context)
    }

    private fun snapshotWithResources(
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

    // region — Empty States

    @Test
    fun emptyState_defaultCredits() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(creditBalance = 0),
                onAction = {}
            )
        }
    }

    @Test
    fun emptyState_withCredits() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(creditBalance = 25),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Photos Selected

    @Test
    fun photosSelected_onePhoto_noStyle() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(1),
                    creditBalance = 10
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun photosSelected_threePhotos_noStyle() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(3),
                    creditBalance = 10
                ),
                onAction = {}
            )
        }
    }

    @Test
    fun photosSelected_threePhotos_withStyle() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(3),
                    selectedStyle = fakeStyle(),
                    creditBalance = 10
                ),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Max Photos

    @Test
    fun maxPhotos_readyToGenerate() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(HomeUiState.MAX_PHOTOS),
                    selectedStyle = fakeStyle(),
                    exportFormat = ExportFormat.ETSY_SQUARE,
                    creditBalance = 50
                ),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Export Formats

    @Test
    fun exportFormat_etsySelected() {
        snapshotWithResources {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(2),
                    selectedStyle = fakeStyle(),
                    exportFormat = ExportFormat.ETSY_SQUARE,
                    creditBalance = 10
                ),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Dark Theme

    @Test
    fun emptyState_darkTheme() {
        snapshotWithResources(darkTheme = true) {
            HomeScreenContent(
                state = HomeUiState(creditBalance = 15),
                onAction = {}
            )
        }
    }

    @Test
    fun photosSelected_darkTheme() {
        snapshotWithResources(darkTheme = true) {
            HomeScreenContent(
                state = HomeUiState(
                    photos = fakePhotos(3),
                    selectedStyle = fakeStyle(),
                    creditBalance = 25
                ),
                onAction = {}
            )
        }
    }

    // endregion

    // region — Helpers

    private fun fakePhotos(count: Int): List<ProductPhoto> =
        (1..count).map { ProductPhoto(id = "photo_$it", localUri = "file:///fake/photo_$it.jpg") }

    private fun fakeStyle() = Style(
        id = "rustic_wood",
        displayName = UiText.DynamicString("rustic_wood"),
        categories = emptySet(),
        thumbnail = null,
        kontextPrompt = "Change only the background to rustic wood."
    )

    // endregion
}
