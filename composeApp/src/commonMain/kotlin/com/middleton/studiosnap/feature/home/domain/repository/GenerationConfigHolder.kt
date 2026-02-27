package com.middleton.studiosnap.feature.home.domain.repository

import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig

/**
 * In-memory holder for passing GenerationConfig between screens.
 * Home writes it before navigating, Processing reads it on init.
 * Similar pattern to RestorationRepository.currentResult in Restorer AI.
 */
interface GenerationConfigHolder {
    var currentConfig: GenerationConfig?
}

class GenerationConfigHolderImpl : GenerationConfigHolder {
    override var currentConfig: GenerationConfig? = null
}
