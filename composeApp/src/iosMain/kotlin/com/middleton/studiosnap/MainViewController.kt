package com.middleton.studiosnap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.middleton.studiosnap.purchases.PurchasesManager

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Disable iOS swipe-back gesture globally.
        // Swipe conflicts with before/after slider drag gestures.
        // All screens have explicit back/close buttons instead.
        enableBackGesture = false
    }
) {
    // Initialize RevenueCat SDK
    PurchasesManager.configure()

    App()
}
