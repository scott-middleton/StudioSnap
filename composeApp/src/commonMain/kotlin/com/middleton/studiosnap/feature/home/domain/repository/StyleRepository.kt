package com.middleton.studiosnap.feature.home.domain.repository

import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory

interface StyleRepository {
    fun getAllStyles(): List<Style>
    fun getStylesByCategory(category: StyleCategory): List<Style>
    fun getStyleById(id: String): Style?
}
