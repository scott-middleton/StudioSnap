package com.middleton.studiosnap.core.domain.service

class FakeAnalyticsService : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    override fun logEvent(name: String, params: Map<String, Any>) {
        loggedEvents.add(name to params)
    }

    fun hasEvent(name: String): Boolean = loggedEvents.any { it.first == name }

    fun clear() = loggedEvents.clear()
}
