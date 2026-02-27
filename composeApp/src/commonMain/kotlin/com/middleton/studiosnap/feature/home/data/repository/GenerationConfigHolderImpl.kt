package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder

// TODO: Register as singleton in Koin module during Phase 3 DI wiring
class GenerationConfigHolderImpl : GenerationConfigHolder {
    override var currentConfig: GenerationConfig? = null
}
