package com.middleton.studiosnap.core.domain.service

import com.middleton.studiosnap.core.domain.model.UserCredits

interface CreditDeductor {
    suspend fun deductGenerationCredit(idempotencyKey: String): Result<UserCredits>
    suspend fun refundGenerationCredit(idempotencyKey: String): Result<UserCredits>
}
