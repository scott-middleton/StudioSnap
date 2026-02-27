package com.middleton.studiosnap.core.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/studiosnap.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
