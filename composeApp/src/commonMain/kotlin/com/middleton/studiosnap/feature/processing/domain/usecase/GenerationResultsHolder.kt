package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult

/**
 * In-memory holder for passing generation results from Processing to Results screen.
 * Processing writes results + credit refund count, Results reads on init.
 */
interface GenerationResultsHolder {
    var currentResults: List<GenerationResult>?
    var refundedCredits: Int
}
