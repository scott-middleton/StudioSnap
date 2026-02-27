package com.middleton.studiosnap.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.presentation.theme.ImageCloneAiTheme
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.core.presentation.imagepicker.ImagePickerResult
import com.middleton.studiosnap.feature.mainrestore.presentation.MainRestoreScreenContent
import com.middleton.studiosnap.feature.mainrestore.presentation.ui_state.MainRestoreUiState
import com.middleton.studiosnap.feature.mainrestore.presentation.ui_state.UserCreditLoadingState

// Mock ImagePickerResult for preview states that show a selected photo
private val mockImagePickerResult = ImagePickerResult(
    uri = "content://mock/image.jpg",
    width = 1920,
    height = 1080,
    fileName = "mock_image.jpg",
    fileSize = 1024L,
    mimeType = "image/jpeg"
)

// Mock data generators
private fun createMockRecentRestorations(): List<RecentRestoration> {
    val currentTime = 1703084400000L // Fixed timestamp for preview
    return listOf(
        RecentRestoration(
            id = "rest_1",
            originalImagePath = "original_1.jpg",
            restoredImagePath = "restored_1.jpg",
            thumbnailPath = "thumb_1.jpg",
            restoreDate = currentTime - (1000 * 60 * 60 * 24), // 1 day ago
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        ),
        RecentRestoration(
            id = "rest_2",
            originalImagePath = "original_2.jpg",
            restoredImagePath = "restored_2.jpg",
            thumbnailPath = "thumb_2.jpg",
            restoreDate = currentTime - (1000 * 60 * 60 * 24 * 3), // 3 days ago
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        ),
        RecentRestoration(
            id = "rest_3",
            originalImagePath = "original_3.jpg",
            restoredImagePath = "restored_3.jpg",
            thumbnailPath = "thumb_3.jpg",
            restoreDate = currentTime - (1000 * 60 * 60 * 24 * 7), // 1 week ago
            restoreOptions = PhotoRestoreOptions(),
            tokenCost = 1
        )
    )
}

// Mock UI States
private val mockEmptyState = MainRestoreUiState(
    userCreditLoadingState = UserCreditLoadingState.Loaded(UserCredits(tokenCount = 12)),
    selectedImage = null,
    isLoading = false,
    recentRestorations = createMockRecentRestorations()
)

private val mockWithPhotoState = MainRestoreUiState(
    userCreditLoadingState = UserCreditLoadingState.Loaded(UserCredits(tokenCount = 12)),
    selectedImage = mockImagePickerResult,
    isLoading = false,
    recentRestorations = createMockRecentRestorations()
)

private val mockLoadingState = MainRestoreUiState(
    userCreditLoadingState = UserCreditLoadingState.Loading,
    selectedImage = null,
    isLoading = true,
    recentRestorations = createMockRecentRestorations()
)

private val mockLowCreditsState = MainRestoreUiState(
    userCreditLoadingState = UserCreditLoadingState.Loaded(UserCredits(tokenCount = 0)),
    selectedImage = mockImagePickerResult,
    isLoading = false,
    recentRestorations = createMockRecentRestorations()
)

@Preview(showBackground = true, name = "Main Restore - Empty State")
@Composable
fun MainRestoreScreenEmptyPreview() {
    ImageCloneAiTheme {
        MainRestoreScreenContent(
            uiState = mockEmptyState,
            onAction = { /* Mock action handler */ }
        )
    }
}

@Preview(showBackground = true, name = "Main Restore - Empty State Dark")
@Composable
fun MainRestoreScreenEmptyDarkPreview() {
    ImageCloneAiTheme(darkTheme = true) {
        MainRestoreScreenContent(
            uiState = mockEmptyState,
            onAction = { /* Mock action handler */ }
        )
    }
}

@Preview(showBackground = true, name = "Main Restore - With Photo")
@Composable
fun MainRestoreScreenWithPhotoPreview() {
    ImageCloneAiTheme {
        MainRestoreScreenContent(
            uiState = mockWithPhotoState,
            onAction = { /* Mock action handler */ }
        )
    }
}

@Preview(showBackground = true, name = "Main Restore - With Photo Dark")
@Composable
fun MainRestoreScreenWithPhotoDarkPreview() {
    ImageCloneAiTheme(darkTheme = true) {
        MainRestoreScreenContent(
            uiState = mockWithPhotoState,
            onAction = { /* Mock action handler */ }
        )
    }
}

@Preview(showBackground = true, name = "Main Restore - Loading")
@Composable
fun MainRestoreScreenLoadingPreview() {
    ImageCloneAiTheme {
        MainRestoreScreenContent(
            uiState = mockLoadingState,
            onAction = { /* Mock action handler */ }
        )
    }
}

@Preview(showBackground = true, name = "Main Restore - Low Credits")
@Composable
fun MainRestoreScreenLowCreditsPreview() {
    ImageCloneAiTheme {
        MainRestoreScreenContent(
            uiState = mockLowCreditsState,
            onAction = { /* Mock action handler */ }
        )
    }
}
