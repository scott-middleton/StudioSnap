package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.core.data.analytics.CrashlyticsErrorReporter
import com.middleton.studiosnap.core.data.analytics.FirebaseAnalyticsService
import com.middleton.studiosnap.core.data.cache.ImageCacheManager
import com.middleton.studiosnap.core.data.database.AppDatabase
import com.middleton.studiosnap.core.data.database.GenerationDao
import com.middleton.studiosnap.core.data.database.UserPreferencesDao
import com.middleton.studiosnap.core.data.datasource.KontextRemoteDataSource
import com.middleton.studiosnap.core.data.datasource.KontextRemoteDataSourceImpl
import com.middleton.studiosnap.core.data.datasource.VirtualCurrencyRemoteDataSource
import com.middleton.studiosnap.core.data.datasource.VirtualCurrencyRemoteDataSourceImpl
import com.middleton.studiosnap.core.data.http.HttpClientProvider
import com.middleton.studiosnap.core.data.repository.PurchaseRepositoryImpl
import com.middleton.studiosnap.core.data.repository.UserPreferencesRepositoryImpl
import com.middleton.studiosnap.core.data.service.CreditManagerImpl
import com.middleton.studiosnap.core.data.service.CreditRepository
import com.middleton.studiosnap.core.data.service.WatermarkServiceImpl
import com.middleton.studiosnap.core.domain.repository.PurchaseRepository
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.service.CreditQueries
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.core.domain.service.WatermarkService
import com.middleton.studiosnap.purchases.PurchasesIdentifier
import com.middleton.studiosnap.purchases.PurchasesIdentifierImpl
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreDataModule = module {

    single<AnalyticsService> {
        FirebaseAnalyticsService()
    }

    single<ErrorReporter> {
        CrashlyticsErrorReporter()
    }

    single<HttpClient> {
        HttpClientProvider.create()
    }

    single { ImageCacheManager() }

    single<GenerationDao> {
        get<AppDatabase>().generationDao()
    }

    single<UserPreferencesDao> {
        get<AppDatabase>().userPreferencesDao()
    }

    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(get())
    }

    single<KontextRemoteDataSource> {
        KontextRemoteDataSourceImpl(
            httpClient = get(),
            imageCacheManager = get()
        )
    }

    single<WatermarkService> {
        WatermarkServiceImpl()
    }

    factory<VirtualCurrencyRemoteDataSource> {
        VirtualCurrencyRemoteDataSourceImpl(
            authService = get()
        )
    }

    single<CreditManager> {
        CreditManagerImpl(
            remoteDataSource = get()
        )
    }

    single {
        CreditRepository(
            remoteDataSource = get(),
            creditManager = get()
        )
    }

    factory<CreditQueries> { get<CreditRepository>() }
    factory<CreditDeductor> { get<CreditRepository>() }

    factory<PurchaseRepository> {
        PurchaseRepositoryImpl(
            creditDataSource = get()
        )
    }

    factory<PurchasesIdentifier> {
        PurchasesIdentifierImpl()
    }
}
