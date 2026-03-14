package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.core.data.auth.FirebaseAuthService
import com.middleton.studiosnap.core.domain.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Dependency injection module for Firebase Auth configuration
 *
 * Provides:
 * - AuthService implementation using Firebase Auth
 *
 * Note: NativeAuthProvider is provided by platform-specific Koin modules
 * (Android provides with Context, iOS provides without)
 */
val firebaseAuthModule = module {
    single<AuthService> {
        FirebaseAuthService(
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }
}