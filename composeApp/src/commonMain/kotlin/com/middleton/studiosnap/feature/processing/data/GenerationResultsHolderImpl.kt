package com.middleton.studiosnap.feature.processing.data

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder

class GenerationResultsHolderImpl : GenerationResultsHolder {
    override var currentResults: List<GenerationResult>? = null
    override var refundedCredits: Int = 0
}
