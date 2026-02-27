package com.middleton.studiosnap.core.domain.service

/**
 * Domain-layer abstraction for error reporting.
 * Implementations can log to Crashlytics, console, etc.
 */
interface ErrorReporter {
    fun recordException(exception: Throwable)
}
