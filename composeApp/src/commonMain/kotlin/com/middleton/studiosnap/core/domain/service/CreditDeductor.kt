package com.middleton.studiosnap.core.domain.service

import com.middleton.studiosnap.core.domain.model.UserCredits

interface CreditDeductor {
    suspend fun deductCredits(amount: Int, reason: String): Result<UserCredits>
    suspend fun refundCredits(amount: Int, reason: String): Result<UserCredits>
}
