package com.middleton.studiosnap.feature.home.presentation.action

import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

sealed interface StylePickerUiAction {
    data class OnCategorySelected(val category: StyleCategory) : StylePickerUiAction
    data class OnInitialise(val confirmedStyleId: String?) : StylePickerUiAction
    data class OnStylePreviewed(val styleId: String) : StylePickerUiAction
}
