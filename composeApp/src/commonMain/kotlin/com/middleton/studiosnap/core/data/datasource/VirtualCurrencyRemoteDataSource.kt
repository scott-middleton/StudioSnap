package com.middleton.studiosnap.core.data.datasource

interface VirtualCurrencyRemoteDataSource {
    suspend fun fetchUserCredits(): Result<Int>
    suspend fun deductGenerationCredit(idempotencyKey: String): Result<Int>
    suspend fun refundGenerationCredit(idempotencyKey: String): Result<Int>
    suspend fun claimWelcomeCredits(): Result<Boolean>
}
