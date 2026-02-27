package com.middleton.studiosnap.core.debug

// TODO: Remove this file when mock restoration mode is no longer needed.
// Also remove:
//   - IS_DEBUG from buildkonfig in composeApp/build.gradle.kts
//   - ToggleMockMode action from MainRestoreUiAction.kt
//   - Mock mode menu item from MainRestoreScreen.kt (search "MockRestorationConfig")
//   - Mock bypass in ProcessingViewModel.kt (search "MockRestorationConfig")
//   - isMockMode from MainRestoreUiState.kt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory toggle for mock restoration mode (debug only).
 * Skips real API calls and simulates the restoration pipeline.
 * Not persisted — resets on app restart.
 */
object MockRestorationConfig {
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun toggle() {
        _enabled.value = !_enabled.value
    }

    val isEnabled: Boolean get() = _enabled.value
}
