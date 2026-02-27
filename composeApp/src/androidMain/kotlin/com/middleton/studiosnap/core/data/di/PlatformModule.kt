package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.core.data.database.AppDatabase
import com.middleton.studiosnap.core.data.database.MIGRATION_3_4
import com.middleton.studiosnap.core.data.database.getDatabaseBuilder
import com.middleton.studiosnap.core.data.repository.AndroidGalleryRepository
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.AndroidRatingService
import com.middleton.studiosnap.core.domain.service.RatingService
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific modules
 */
actual fun platformModule(): Module = module {
    single<AppDatabase> {
        getDatabaseBuilder()
            .addMigrations(MIGRATION_3_4)
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    single<GalleryRepository> {
        AndroidGalleryRepository()
    }

    single<RatingService> {
        AndroidRatingService()
    }
}
