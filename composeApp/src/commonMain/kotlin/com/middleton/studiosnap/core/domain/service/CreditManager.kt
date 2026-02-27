package com.middleton.studiosnap.core.domain.service

import com.middleton.studiosnap.core.domain.model.UserCredits
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the user's credit balance with in-memory caching.
 *
 * Credits are cached and exposed as a StateFlow that ViewModels can observe.
 * The cache is refreshed:
 * - Once when the user signs in (or session is restored at app launch)
 * - After a successful purchase
 * - After a credit deduction
 */
interface CreditManager {
    /**
     * Observable state of the user's credits.
     * Emits null when credits haven't been loaded yet or user is signed out.
     */
    val credits: StateFlow<UserCredits?>

    /**
     * Whether credits are currently being loaded.
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Loads credits from the remote source and updates the cache.
     * Call this after sign-in or when session is restored.
     */
    suspend fun loadCredits(): Result<UserCredits>

    /**
     * Forces a refresh of credits from the remote source.
     * Call this after purchases or deductions.
     */
    suspend fun refreshCredits(): Result<UserCredits>

    /**
     * Clears the cached credits.
     * Call this on sign-out.
     */
    fun clearCredits()
}
