package com.middleton.studiosnap.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun setHasCompletedOnboarding()
    suspend fun hasPurchasedCredits(): Boolean
    suspend fun setHasPurchasedCredits()
    suspend fun getFreeDownloadsUsed(): Int
    suspend fun incrementFreeDownloads()
    suspend fun incrementAndGetPaidDownloads(): Int
    suspend fun getPreferredQuality(): String
    suspend fun setPreferredQuality(quality: String)
    suspend fun getLastUsedCategoryFilter(): String
    suspend fun setLastUsedCategoryFilter(category: String)
    fun observePreferences(): Flow<UserPreferencesSnapshot>
}

data class UserPreferencesSnapshot(
    val hasCompletedOnboarding: Boolean,
    val hasPurchasedCredits: Boolean,
    val freeDownloadsUsed: Int,
    val totalPaidDownloads: Int,
    val preferredQuality: String,
    val lastUsedCategoryFilter: String
)
