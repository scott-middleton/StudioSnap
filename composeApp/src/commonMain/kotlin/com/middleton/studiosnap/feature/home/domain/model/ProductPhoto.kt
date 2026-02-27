package com.middleton.studiosnap.feature.home.domain.model

data class ProductPhoto(
    val id: String,
    val localUri: String,
    val compressedUri: String? = null,
    val width: Int = 0,
    val height: Int = 0
)
