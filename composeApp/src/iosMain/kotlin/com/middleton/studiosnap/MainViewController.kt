package com.middleton.studiosnap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Disable iOS swipe-back gesture globally.
        // Swipe conflicts with before/after slider drag gestures.
        // All screens have explicit back/close buttons instead.
        enableBackGesture = false
    }
) {
    App()
}
