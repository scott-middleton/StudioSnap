package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder

class GenerationConfigHolderImpl : GenerationConfigHolder {
    override var currentConfig: GenerationConfig? = null
}
