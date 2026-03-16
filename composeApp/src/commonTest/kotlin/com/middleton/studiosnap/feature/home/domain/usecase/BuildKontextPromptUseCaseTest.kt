package com.middleton.studiosnap.feature.home.domain.usecase

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildKontextPromptUseCaseTest {

    private val useCase = BuildKontextPromptUseCase()

    private val testStyle = Style(
        id = "test",
        displayName = UiText.DynamicString("Test Style"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "Base prompt here."
    )

    @Test
    fun `returns base prompt when no shadow or reflection`() {
        val result = useCase(testStyle, shadow = false, reflection = false)
        assertEquals("Base prompt here.", result)
    }

    @Test
    fun `appends shadow suffix when shadow enabled`() {
        val result = useCase(testStyle, shadow = true, reflection = false)
        assertTrue(result.startsWith("Base prompt here."))
        assertTrue(result.contains("shadow"))
        assertFalse(result.contains("reflection"))
    }

    @Test
    fun `appends reflection suffix when reflection enabled`() {
        val result = useCase(testStyle, shadow = false, reflection = true)
        assertTrue(result.startsWith("Base prompt here."))
        assertTrue(result.contains("reflection"))
        assertFalse(result.contains("shadow"))
    }

    @Test
    fun `appends both shadow and reflection when both enabled`() {
        val result = useCase(testStyle, shadow = true, reflection = true)
        assertTrue(result.startsWith("Base prompt here."))
        assertTrue(result.contains("shadow"))
        assertTrue(result.contains("reflection"))
    }

    @Test
    fun `shadow comes before reflection in final prompt`() {
        val result = useCase(testStyle, shadow = true, reflection = true)
        val shadowIndex = result.indexOf("shadow")
        val reflectionIndex = result.indexOf("reflection")
        assertTrue(shadowIndex < reflectionIndex)
    }
}
