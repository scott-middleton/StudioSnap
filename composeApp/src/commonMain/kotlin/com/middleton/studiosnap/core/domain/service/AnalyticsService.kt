package com.middleton.studiosnap.core.domain.service

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
}

object AnalyticsEvents {
    // Conversion funnel
    const val APP_FIRST_OPEN = "app_first_open"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val CREDIT_STORE_VIEWED = "credit_store_viewed"
    const val PACK_SELECTED = "pack_selected"
    const val PURCHASE_STARTED = "purchase_started"
    const val PURCHASE_COMPLETED = "purchase_completed"
    const val PURCHASE_FAILED = "purchase_failed"

    // Photo & Style
    const val PHOTO_ADDED = "photo_added"
    const val STYLE_SELECTED = "style_selected"
    const val EXPORT_FORMAT_SELECTED = "export_format_selected"

    // Generation
    const val PREVIEW_GENERATION_STARTED = "preview_generation_started"
    const val BATCH_GENERATION_COMPLETED = "batch_generation_completed"
    const val BATCH_GENERATION_FAILED = "batch_generation_failed"

    // Download & Share
    const val DOWNLOAD_COMPLETED = "download_completed"
    const val DOWNLOAD_FAILED = "download_failed"
    const val PREVIEW_SHARED = "preview_shared"

    // Engagement
    const val HISTORY_VIEWED = "history_viewed"
    const val SETTINGS_VIEWED = "settings_viewed"
    const val SIGN_IN_COMPLETED = "sign_in_completed"
}

object AnalyticsParams {
    const val PACK_NAME = "pack_name"
    const val PACK_CREDITS = "pack_credits"
    const val STYLE_ID = "style_id"
    const val PHOTO_COUNT = "photo_count"
    const val EXPORT_FORMAT = "export_format"
    const val ERROR_TYPE = "error_type"
    const val GENERATION_ID = "generation_id"
}
