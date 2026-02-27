package com.middleton.studiosnap.core.data.service

import com.middleton.studiosnap.core.data.datasource.VirtualCurrencyRemoteDataSource
import com.middleton.studiosnap.core.data.mapper.toUserCredits
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.CreditQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class CreditRepository(
    private val remoteDataSource: VirtualCurrencyRemoteDataSource,
    private val creditManager: CreditManager
) : CreditQueries, CreditDeductor {

    override fun observeCredits(): Flow<UserCredits> {
        return creditManager.credits.filterNotNull()
    }

    override suspend fun getUserCredits(): Result<UserCredits> {
        return creditManager.loadCredits()
    }

    override suspend fun refreshCredits(): Result<UserCredits> {
        return creditManager.refreshCredits()
    }

    override suspend fun deductCredits(amount: Int, reason: String): Result<UserCredits> {
        return remoteDataSource.deductCredits(amount, reason)
            .map { newBalance -> newBalance.toUserCredits() }
            .onSuccess {
                creditManager.refreshCredits()
            }
    }

    override suspend fun refundCredits(amount: Int, reason: String): Result<UserCredits> {
        return remoteDataSource.addCredits(amount, reason)
            .map { newBalance -> newBalance.toUserCredits() }
            .onSuccess {
                creditManager.refreshCredits()
            }
    }
}
