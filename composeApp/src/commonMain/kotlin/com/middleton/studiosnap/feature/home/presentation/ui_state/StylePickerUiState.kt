package com.middleton.studiosnap.feature.home.presentation.ui_state

import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

data class StylePickerUiState(
    val styles: List<Style> = emptyList(),
    val selectedCategory: StyleCategory = StyleCategory.ALL
)
