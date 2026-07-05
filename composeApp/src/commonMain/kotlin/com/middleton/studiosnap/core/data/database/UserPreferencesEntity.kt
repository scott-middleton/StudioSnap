package com.middleton.studiosnap.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val hasCompletedOnboarding: Boolean = false,
    val hasPurchasedCredits: Boolean = false,
    val freeDownloadsUsed: Int = 0,
    val totalPaidDownloads: Int = 0,
    val preferredQuality: String = "HIGH",
    val lastUsedCategoryFilter: String = "ALL"
)
