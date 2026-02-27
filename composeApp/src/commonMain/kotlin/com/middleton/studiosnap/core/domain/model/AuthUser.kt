package com.middleton.studiosnap.core.domain.model

/**
 * Represents an authenticated user in the application
 */
data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val provider: AuthProvider
)

/**
 * Authentication provider type
 */
enum class AuthProvider {
    APPLE,
    GOOGLE,
    ANONYMOUS
}
