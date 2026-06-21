package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

data class StylePickerUiState(
    val styles: List<Style> = emptyList(),
    val allStyles: List<Style> = emptyList(),
    val selectedCategory: StyleCategory = StyleCategory.ALL,
    val confirmedStyleId: String? = null,
    // Intentionally not SavedStateHandle-backed: an in-progress preview should NOT
    // survive process death — it should fall back to confirmedStyleId, which is the
    // desired "discard" behavior when the user never taps "Use this style".
    val previewedStyleId: String? = null
) {
    val heroStyleId: String? get() = previewedStyleId ?: confirmedStyleId
    val isHeroUnconfirmedPreview: Boolean get() = previewedStyleId != null && previewedStyleId != confirmedStyleId
}
