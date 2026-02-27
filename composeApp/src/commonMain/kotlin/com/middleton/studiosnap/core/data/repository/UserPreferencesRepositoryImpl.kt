package com.middleton.studiosnap.core.data.repository

import com.middleton.studiosnap.core.data.database.UserPreferencesDao
import com.middleton.studiosnap.core.data.database.UserPreferencesEntity
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserPreferencesRepositoryImpl(
    private val dao: UserPreferencesDao
) : UserPreferencesRepository {

    private var initialized = false
    private val mutex = Mutex()

    private suspend fun ensureExists() {
        if (!initialized) {
            mutex.withLock {
                if (!initialized) {
                    dao.insertDefault(UserPreferencesEntity())
                    initialized = true
                }
            }
        }
    }

    override suspend fun hasCompletedOnboarding(): Boolean {
        ensureExists()
        return dao.getPreferences()?.hasCompletedOnboarding ?: false
    }

    override suspend fun setHasCompletedOnboarding() {
        ensureExists()
        dao.setHasCompletedOnboarding()
    }

    override suspend fun hasPurchasedCredits(): Boolean {
        ensureExists()
        return dao.getPreferences()?.hasPurchasedCredits ?: false
    }

    override suspend fun setHasPurchasedCredits() {
        ensureExists()
        dao.setHasPurchasedCredits()
    }

    override suspend fun getFreeDownloadsUsed(): Int {
        ensureExists()
        return dao.getPreferences()?.freeDownloadsUsed ?: 0
    }

    override suspend fun incrementFreeDownloads() {
        ensureExists()
        dao.incrementFreeDownloads()
    }

    override suspend fun incrementAndGetPaidDownloads(): Int {
        ensureExists()
        return dao.incrementAndGetPaidDownloads()
    }

    override suspend fun getPreferredQuality(): String {
        ensureExists()
        return dao.getPreferences()?.preferredQuality ?: "HIGH"
    }

    override suspend fun setPreferredQuality(quality: String) {
        ensureExists()
        dao.setPreferredQuality(quality)
    }

    override suspend fun getLastUsedCategoryFilter(): String {
        ensureExists()
        return dao.getPreferences()?.lastUsedCategoryFilter ?: "ALL"
    }

    override suspend fun setLastUsedCategoryFilter(category: String) {
        ensureExists()
        dao.setLastUsedCategoryFilter(category)
    }

    override fun observePreferences(): Flow<UserPreferencesSnapshot> {
        return dao.observePreferences().map { entity ->
            val prefs = entity ?: UserPreferencesEntity()
            UserPreferencesSnapshot(
                hasCompletedOnboarding = prefs.hasCompletedOnboarding,
                hasPurchasedCredits = prefs.hasPurchasedCredits,
                freeDownloadsUsed = prefs.freeDownloadsUsed,
                totalPaidDownloads = prefs.totalPaidDownloads,
                preferredQuality = prefs.preferredQuality,
                lastUsedCategoryFilter = prefs.lastUsedCategoryFilter
            )
        }
    }
}
