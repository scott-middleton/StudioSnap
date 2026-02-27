package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.core.data.database.AppDatabase
import com.middleton.studiosnap.core.data.database.MIGRATION_3_4
import com.middleton.studiosnap.core.data.database.getDatabaseBuilder
import com.middleton.studiosnap.core.data.repository.IosGalleryRepository
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.IosRatingService
import com.middleton.studiosnap.core.domain.service.RatingService
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific modules
 */
actual fun platformModule(): Module = module {
    single<AppDatabase> {
        getDatabaseBuilder()
            .addMigrations(MIGRATION_3_4)
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    single<GalleryRepository> {
        IosGalleryRepository()
    }

    single<RatingService> {
        IosRatingService()
    }
}
