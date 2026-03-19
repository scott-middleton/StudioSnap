package com.middleton.studiosnap.core.presentation.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// TODO: Replace hardcoded English strings with localized string resources when i18n is added
fun formatRelativeTime(createdAt: Long, nowMs: Long = Clock.System.now().toEpochMilliseconds()): String {
    val diffMs = nowMs - createdAt
    val diffDays = diffMs / (1000L * 60 * 60 * 24)
    return when {
        diffDays == 0L -> "Today"
        diffDays == 1L -> "Yesterday"
        diffDays < 7L -> "$diffDays days ago"
        diffDays < 14L -> "Last week"
        else -> {
            val local = Instant.fromEpochMilliseconds(createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val monthName = when (local.monthNumber) {
                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
                5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec"
            }
            "${local.dayOfMonth} $monthName"
        }
    }
}
