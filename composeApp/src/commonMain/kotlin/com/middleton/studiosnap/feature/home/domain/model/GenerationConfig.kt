package com.middleton.studiosnap.feature.home.domain.model

/**
 * Everything needed to kick off a generation job.
 * Built from user selections on the Home screen.
 *
 * The pipeline's unit of work is a (photo, style) pair. Home's constrained modes guarantee
 * at most one of [photos] / [styles] has more than one element at a time: up to 10 photos ×
 * exactly 1 style (the original flow), or exactly 1 photo × up to 4 styles.
 */
data class GenerationConfig(
    val photos: List<ProductPhoto>,
    val styles: List<Style>,
    val shadow: Boolean,
    val reflection: Boolean,
    val exportFormat: ExportFormat,
    val quality: GenerationQuality,
    val batchId: String,
    val isFreeGeneration: Boolean = false
) {
    /** A single (photo, style) pair — the pipeline's atomic unit of work. */
    data class GenerationUnit(val photo: ProductPhoto, val style: Style)

    /** Cartesian product, photo-major: all styles for photo 0, then all styles for photo 1, ... */
    val units: List<GenerationUnit> get() = photos.flatMap { p -> styles.map { s -> GenerationUnit(p, s) } }

    val unitCount: Int get() = photos.size * styles.size
}
