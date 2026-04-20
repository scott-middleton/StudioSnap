package com.middleton.studiosnap.core.data.service

import com.middleton.studiosnap.core.data.datasource.CloudFunctionDataSource
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.FreeGenerationGate

class FreeGenerationGateImpl(
    private val cloudFunctions: CloudFunctionDataSource,
    private val userPreferencesRepository: UserPreferencesRepository
) : FreeGenerationGate {

    override suspend fun checkFreeGenerationUsed(): Boolean {
        val used = cloudFunctions.checkFreeGenerationUsed()
        if (used) userPreferencesRepository.setHasUsedFreeGeneration()
        return used
    }

    override suspend fun claimFreeGeneration(): Boolean {
        val claimed = cloudFunctions.claimFreeGeneration()
        if (!claimed) userPreferencesRepository.setHasUsedFreeGeneration()
        return claimed
    }
}
