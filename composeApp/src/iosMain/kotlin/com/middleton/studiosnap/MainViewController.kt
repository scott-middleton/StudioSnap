package com.middleton.studiosnap

import androidx.compose.ui.window.ComposeUIViewController
import com.middleton.studiosnap.purchases.PurchasesManager

fun MainViewController() = ComposeUIViewController {
    // Initialize RevenueCat SDK
    PurchasesManager.configure()

    App()
}