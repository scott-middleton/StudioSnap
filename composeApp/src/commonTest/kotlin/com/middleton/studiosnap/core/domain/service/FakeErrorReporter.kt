package com.middleton.studiosnap.core.domain.service

class FakeErrorReporter : ErrorReporter {
    val recordedExceptions = mutableListOf<Throwable>()
    override fun recordException(exception: Throwable) {
        recordedExceptions.add(exception)
    }
}
