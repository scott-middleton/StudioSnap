package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

/**
 * In-memory holder for passing generation results from Processing to Results screen.
 * Processing writes results, Results reads on init.
 */
interface GenerationResultsHolder {
    var currentResults: List<GenerationResult>?
}

class GenerationResultsHolderImpl : GenerationResultsHolder {
    override var currentResults: List<GenerationResult>? = null
}
