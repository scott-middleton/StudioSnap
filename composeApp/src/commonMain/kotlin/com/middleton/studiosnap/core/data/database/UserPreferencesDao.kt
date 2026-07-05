package com.middleton.studiosnap.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferences(): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun observePreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(entity: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET hasCompletedOnboarding = 1 WHERE id = 1")
    suspend fun setHasCompletedOnboarding()

    @Query("UPDATE user_preferences SET hasPurchasedCredits = 1 WHERE id = 1")
    suspend fun setHasPurchasedCredits()

    @Query("UPDATE user_preferences SET freeDownloadsUsed = freeDownloadsUsed + 1 WHERE id = 1")
    suspend fun incrementFreeDownloads()

    @Query("UPDATE user_preferences SET totalPaidDownloads = totalPaidDownloads + 1 WHERE id = 1")
    suspend fun incrementPaidDownloads()

    @Query("UPDATE user_preferences SET lastUsedCategoryFilter = :category WHERE id = 1")
    suspend fun setLastUsedCategoryFilter(category: String)

    @Transaction
    suspend fun incrementAndGetPaidDownloads(): Int {
        incrementPaidDownloads()
        val prefs = getPreferences()
        return prefs?.totalPaidDownloads ?: 0
    }
}
