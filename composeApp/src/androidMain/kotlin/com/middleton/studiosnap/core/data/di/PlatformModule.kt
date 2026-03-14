package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.core.data.auth.NativeAuthProvider
import com.middleton.studiosnap.core.data.database.AppDatabase
import com.middleton.studiosnap.core.data.database.getDatabaseBuilder
import com.middleton.studiosnap.core.data.repository.AndroidGalleryRepository
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.AndroidRatingService
import com.middleton.studiosnap.core.domain.service.RatingService
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific modules — StudioSnap
 * Fresh Room v1 schema, no migrations needed.
 */
actual fun platformModule(): Module = module {
    single<AppDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<GalleryRepository> {
        AndroidGalleryRepository()
    }

    single<RatingService> {
        AndroidRatingService()
    }

    single<NativeAuthProvider> {
        NativeAuthProvider(
            context = AndroidContextHolder.context!!
        )
    }
}
