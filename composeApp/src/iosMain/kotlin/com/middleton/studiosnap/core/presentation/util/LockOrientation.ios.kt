package com.middleton.studiosnap.core.presentation.util

import androidx.compose.runtime.Composable

@Composable
actual fun LockLandscapeOrientation() {
    // No-op on iOS: UIWindowSceneGeometryPreferencesIOS requires iOS 16+
    // and referencing it directly causes a dyld symbol crash on older devices.
    // Landscape lock is a non-critical enhancement — skip on iOS.
}
