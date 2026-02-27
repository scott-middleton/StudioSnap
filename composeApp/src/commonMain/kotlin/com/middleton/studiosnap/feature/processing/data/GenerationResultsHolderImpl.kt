package com.middleton.studiosnap.feature.processing.data

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder

// TODO: Register as singleton in Koin module during Phase 3 DI wiring
class GenerationResultsHolderImpl : GenerationResultsHolder {
    override var currentResults: List<GenerationResult>? = null
}
