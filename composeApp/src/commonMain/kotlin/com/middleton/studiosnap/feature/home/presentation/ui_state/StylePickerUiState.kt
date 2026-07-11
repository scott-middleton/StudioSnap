package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

data class StylePickerUiState(
    val styles: List<Style> = emptyList(),
    val allStyles: List<Style> = emptyList(),
    val selectedCategory: StyleCategory = StyleCategory.ALL,
    /** Ordered — selection-order badges in multi-select mode reflect this order. */
    val selectedStyleIds: List<String> = emptyList(),
    val maxSelectable: Int = 1,
    // Intentionally not SavedStateHandle-backed: an in-progress preview should NOT
    // survive process death — it should fall back to the confirmed selection, which is the
    // desired "discard" behavior when the user never taps confirm.
    val previewedStyleId: String? = null,
    /** Incremented on a rejected at-cap tap in multi-select mode — the screen pulses the subtitle. */
    val capPulse: Int = 0
) {
    val isMultiSelect: Boolean get() = maxSelectable > 1
    val heroStyleId: String? get() = previewedStyleId ?: selectedStyleIds.lastOrNull()
    val isHeroUnconfirmedPreview: Boolean
        get() = previewedStyleId != null && previewedStyleId !in selectedStyleIds
    val isAtCap: Boolean get() = selectedStyleIds.size >= maxSelectable
}
