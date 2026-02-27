package com.middleton.studiosnap.core.data.service

import com.middleton.studiosnap.core.data.datasource.VirtualCurrencyRemoteDataSource
import com.middleton.studiosnap.core.data.mapper.toUserCredits
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreditManagerImpl(
    private val remoteDataSource: VirtualCurrencyRemoteDataSource
) : CreditManager {

    private val _credits = MutableStateFlow<UserCredits?>(null)
    override val credits: StateFlow<UserCredits?> = _credits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override suspend fun loadCredits(): Result<UserCredits> {
        // If we already have credits cached, return them without fetching
        _credits.value?.let { return Result.success(it) }
        return refreshCredits()
    }

    override suspend fun refreshCredits(): Result<UserCredits> {
        _isLoading.value = true
        return remoteDataSource.fetchUserCredits()
            .map { balance -> balance.toUserCredits() }
            .onSuccess { userCredits ->
                _credits.value = userCredits
            }
            .also {
                _isLoading.value = false
            }
    }

    override fun clearCredits() {
        _credits.value = null
    }
}
