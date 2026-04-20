package com.middleton.studiosnap.core.domain.service

interface FreeGenerationGate {
    suspend fun checkFreeGenerationUsed(): Boolean
    suspend fun claimFreeGeneration(): Boolean
}
