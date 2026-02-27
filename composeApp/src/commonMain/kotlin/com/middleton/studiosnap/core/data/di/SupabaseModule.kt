package com.middleton.studiosnap.core.data.di

import com.middleton.studiosnap.composeapp.BuildKonfig
import com.middleton.studiosnap.core.data.auth.SupabaseAuthService
import com.middleton.studiosnap.core.domain.service.AuthService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Dependency injection module for Supabase configuration
 *
 * Provides:
 * - SupabaseClient configured with Auth and ComposeAuth
 * - AuthService implementation using Supabase
 * - Native Apple/Google sign-in support
 */
val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })

            install(Auth) {
                scheme = BuildKonfig.SCHEME
                host = "auth"
            }

            install(ComposeAuth) {
                googleNativeLogin(serverClientId = BuildKonfig.GOOGLE_SERVER_CLIENT_ID)
                appleNativeLogin()
            }
        }
    }

    single<AuthService> {
        SupabaseAuthService(
            supabaseClient = get(),
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }
}
