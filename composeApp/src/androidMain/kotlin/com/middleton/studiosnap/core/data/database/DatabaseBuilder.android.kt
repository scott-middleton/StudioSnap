package com.middleton.studiosnap.core.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidContextHolder.context
        ?: error("AndroidContextHolder.context must be set before accessing the database")
    val dbFile = context.getDatabasePath("studiosnap.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
