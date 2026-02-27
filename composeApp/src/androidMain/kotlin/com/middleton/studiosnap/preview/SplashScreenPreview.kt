package com.middleton.studiosnap.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.middleton.studiosnap.core.presentation.theme.ImageCloneAiTheme
import com.middleton.studiosnap.feature.splash.presentation.SplashScreen

@Preview(showBackground = true, name = "Photo Restoration Splash")
@Composable
fun SplashScreenPreview() {
    ImageCloneAiTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, name = "Photo Restoration Splash Dark")
@Composable
fun SplashScreenDarkPreview() {
    ImageCloneAiTheme(darkTheme = true) {
        SplashScreen()
    }
}