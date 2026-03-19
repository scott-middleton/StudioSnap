package com.middleton.studiosnap.feature.history.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatRelativeTimeTest {

    // Using a fixed "now" of 2026-03-19 12:00:00 UTC = 1742385600000L
    private val nowMs = 1742385600000L

    private fun format(createdAt: Long) = formatRelativeTime(createdAt, nowMs)

    @Test
    fun `same day returns Today`() {
        val createdAt = nowMs - (2 * 60 * 60 * 1000L) // 2 hours ago
        assertEquals("Today", format(createdAt))
    }

    @Test
    fun `exactly 0 days diff returns Today`() {
        assertEquals("Today", format(nowMs))
    }

    @Test
    fun `1 day ago returns Yesterday`() {
        val createdAt = nowMs - (25 * 60 * 60 * 1000L) // 25 hours ago
        assertEquals("Yesterday", format(createdAt))
    }

    @Test
    fun `6 days ago returns days ago`() {
        val createdAt = nowMs - (6 * 24 * 60 * 60 * 1000L)
        assertEquals("6 days ago", format(createdAt))
    }

    @Test
    fun `7 days ago returns Last week`() {
        val createdAt = nowMs - (7 * 24 * 60 * 60 * 1000L)
        assertEquals("Last week", format(createdAt))
    }

    @Test
    fun `13 days ago returns Last week`() {
        val createdAt = nowMs - (13 * 24 * 60 * 60 * 1000L)
        assertEquals("Last week", format(createdAt))
    }

    @Test
    fun `14 days ago returns formatted date`() {
        val createdAt = nowMs - (14 * 24 * 60 * 60 * 1000L) // 5 Mar 2026
        assertEquals("5 Mar", format(createdAt))
    }

    @Test
    fun `older date returns formatted date`() {
        val createdAt = nowMs - (30 * 24 * 60 * 60 * 1000L) // 17 Feb 2026
        assertEquals("17 Feb", format(createdAt))
    }
}
