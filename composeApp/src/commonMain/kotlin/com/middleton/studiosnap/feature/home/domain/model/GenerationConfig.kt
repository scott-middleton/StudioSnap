package com.middleton.studiosnap.feature.home.domain.model

/**
 * Everything needed to kick off a generation job.
 * Built from user selections on the Home screen.
 *
 * The pipeline's unit of work is a [GenerationUnit] — a (photo, style, resolved prompt)
 * triple. Home's constrained, mutually-exclusive background modes guarantee at most one
 * dimension exceeds 1 at a time:
 *  - up to 10 photos × exactly 1 preset style (the original flow), or
 *  - exactly 1 photo × up to 4 preset styles (multi-select), or
 *  - up to 10 photos × exactly 1 custom text description (single placeholder style).
 *
 * Each unit carries its own [GenerationUnit.resolvedPrompt] (already combined with any
 * shadow/reflection modifiers) so the pipeline never has to know whether a generation
 * came from a preset or a custom description — it just runs the prompt against the photo.
 */
data class GenerationConfig(
    val photos: List<ProductPhoto>,
    val units: List<GenerationUnit>,
    val shadow: Boolean,
    val reflection: Boolean,
    val exportFormat: ExportFormat,
    val quality: GenerationQuality,
    val batchId: String
) {
    /** A single (photo, style, prompt) triple — the pipeline's atomic unit of work. */
    data class GenerationUnit(
        val photo: ProductPhoto,
        val style: Style,
        val resolvedPrompt: String
    )

    /** Distinct styles across all units, in first-seen order. */
    val styles: List<Style> get() = units.map { it.style }.distinctBy { it.id }

    val unitCount: Int get() = units.size
}
