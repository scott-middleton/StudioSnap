package com.middleton.studiosnap.feature.home.presentation.action

import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

sealed interface StylePickerUiAction {
    data class OnCategorySelected(val category: StyleCategory) : StylePickerUiAction
    data class OnInitialise(val currentStyleIds: List<String>, val maxSelectable: Int) : StylePickerUiAction
    /** Single-select mode: tap previews a style without confirming (unchanged behavior). */
    data class OnStylePreviewed(val styleId: String) : StylePickerUiAction
    /** Multi-select mode: tap adds/removes a style from the selection (ignored at cap). */
    data class OnStyleToggled(val styleId: String) : StylePickerUiAction
}
