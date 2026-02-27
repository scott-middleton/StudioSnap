package com.middleton.studiosnap.core.domain.service

import com.middleton.studiosnap.core.domain.model.UserCredits
import kotlinx.coroutines.flow.Flow

interface CreditQueries {
    suspend fun getUserCredits(): Result<UserCredits>
    suspend fun refreshCredits(): Result<UserCredits>
    fun observeCredits(): Flow<UserCredits>
}
