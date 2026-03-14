package com.middleton.studiosnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.middleton.studiosnap.core.data.service.initializeAppCheck
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import com.middleton.studiosnap.purchases.PurchasesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initializeAppCheck()

        // Initialize context holder for image picker and rating prompt
        AndroidContextHolder.context = applicationContext
        AndroidContextHolder.activity = this

        // Initialize RevenueCat SDK for purchases
        // Note: This is only needed for RevenueCat purchase SDK functionality
        PurchasesManager.configure()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidContextHolder.activity = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}