package com.middleton.studiosnap.feature.home.domain.model

/**
 * Controls the number of inference steps for Flux Kontext.
 * More steps = better quality but slower and marginally more expensive.
 */
enum class GenerationQuality(val inferenceSteps: Int) {
    STANDARD(20),
    HIGH(30);

    companion object {
        val DEFAULT = HIGH
    }
}
