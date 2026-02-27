package com.middleton.studiosnap.core.data.analytics

import com.middleton.studiosnap.core.domain.service.ErrorReporter

/**
 * ErrorReporter implementation that delegates to CrashlyticsService.
 */
class CrashlyticsErrorReporter : ErrorReporter {
    override fun recordException(exception: Throwable) {
        CrashlyticsService.recordException(exception)
    }
}
