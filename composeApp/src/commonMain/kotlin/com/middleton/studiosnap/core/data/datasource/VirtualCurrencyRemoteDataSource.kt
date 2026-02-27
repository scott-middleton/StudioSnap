package com.middleton.studiosnap.core.data.datasource

interface VirtualCurrencyRemoteDataSource {
    suspend fun fetchUserCredits(): Result<Int>
    suspend fun deductCredits(amount: Int, reason: String): Result<Int>
    suspend fun addCredits(amount: Int, reason: String): Result<Int>
}
