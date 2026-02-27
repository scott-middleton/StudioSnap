package com.middleton.studiosnap.feature.home.domain.model

/**
 * Everything needed to kick off a generation job.
 * Built from user selections on the Home screen.
 */
data class GenerationConfig(
    val photos: List<ProductPhoto>,
    val style: Style,
    val shadow: Boolean,
    val reflection: Boolean,
    val exportFormat: ExportFormat,
    val quality: GenerationQuality
)
