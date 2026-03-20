package com.middleton.studiosnap.core.data.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [GenerationEntity::class, UserPreferencesEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun generationDao(): GenerationDao
    abstract fun userPreferencesDao(): UserPreferencesDao
}

// Room compiler generates the actual implementation
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
