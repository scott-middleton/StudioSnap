package com.middleton.studiosnap.feature.mainrestore.domain.model

import com.middleton.studiosnap.core.domain.model.UserCredits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserCreditsTest {

    @Test
    fun `hasEnoughTokens should return true when tokens equal required amount`() {
        val credits = UserCredits(tokenCount = 3)

        assertTrue(credits.hasEnoughTokens(3))
    }

    @Test
    fun `hasEnoughTokens should return true when tokens exceed required amount`() {
        val credits = UserCredits(tokenCount = 10)

        assertTrue(credits.hasEnoughTokens(5))
    }

    @Test
    fun `hasEnoughTokens should return false when tokens less than required amount`() {
        val credits = UserCredits(tokenCount = 2)

        assertFalse(credits.hasEnoughTokens(3))
    }

    @Test
    fun `hasEnoughTokens should return false when no tokens and tokens required`() {
        val credits = UserCredits(tokenCount = 0)

        assertFalse(credits.hasEnoughTokens(1))
    }

    @Test
    fun `hasEnoughTokens should return true when no tokens required`() {
        val credits = UserCredits(tokenCount = 0)

        assertTrue(credits.hasEnoughTokens(0))
    }

    @Test
    fun `hasEnoughTokens should handle negative required tokens`() {
        val credits = UserCredits(tokenCount = 5)

        assertTrue(credits.hasEnoughTokens(-1))
    }

    @Test
    fun `copy should work correctly`() {
        val original = UserCredits(tokenCount = 10)
        val modified = original.copy(tokenCount = 20)

        assertEquals(10, original.tokenCount)
        assertEquals(20, modified.tokenCount)
    }

    @Test
    fun `equality should work correctly`() {
        val credits1 = UserCredits(tokenCount = 5)
        val credits2 = UserCredits(tokenCount = 5)
        val credits3 = UserCredits(tokenCount = 10)

        assertEquals(credits1, credits2)
        assertTrue(credits1 == credits2)
        assertFalse(credits1 == credits3)
    }

    @Test
    fun `hasEnoughTokens edge cases`() {
        val credits = UserCredits(tokenCount = 1)

        assertTrue(credits.hasEnoughTokens(1))
        assertFalse(credits.hasEnoughTokens(2))
        assertTrue(credits.hasEnoughTokens(0))
    }
}
