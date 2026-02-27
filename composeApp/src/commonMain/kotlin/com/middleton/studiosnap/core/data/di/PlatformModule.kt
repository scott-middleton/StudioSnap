package com.middleton.studiosnap.core.data.di

import org.koin.core.module.Module

/**
 * Platform-specific modules for dependency injection.
 * Each platform provides its own implementation.
 */
expect fun platformModule(): Module