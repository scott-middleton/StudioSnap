package com.middleton.studiosnap.core.data.datasource

interface VirtualCurrencyRemoteDataSource {
    suspend fun fetchUserCredits(): Result<Int>
    suspend fun deductGenerationCredit(idempotencyKey: String): Result<Int>
    suspend fun refundGenerationCredit(): Result<Int>
    suspend fun checkFreeGenerationUsed(): Result<Boolean>
    suspend fun claimFreeGeneration(): Result<Boolean>
}
