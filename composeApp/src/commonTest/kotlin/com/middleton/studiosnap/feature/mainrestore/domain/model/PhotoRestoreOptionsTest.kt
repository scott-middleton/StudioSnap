package com.middleton.studiosnap.feature.mainrestore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoRestoreOptionsTest {

    @Test
    fun `CREDIT_COST should be 1`() {
        assertEquals(1, PhotoRestoreOptions.CREDIT_COST)
    }

    @Test
    fun `default PhotoRestoreOptions should have all options disabled`() {
        val options = PhotoRestoreOptions()

        assertFalse(options.colorization)
        assertFalse(options.faceEnhancement)
        assertEquals(1, options.upscaleMultiplier)
    }

    @Test
    fun `equality should work correctly`() {
        val options1 = PhotoRestoreOptions()
        val options2 = PhotoRestoreOptions()
        val options3 = PhotoRestoreOptions(colorization = true)

        assertEquals(options1, options2)
        assertTrue(options1 == options2)
        assertTrue(options1 != options3)
    }

    @Test
    fun `copy should work correctly`() {
        val original = PhotoRestoreOptions()
        val withColorization = original.copy(colorization = true)
        val withFaceEnhancement = original.copy(faceEnhancement = true)
        val withUpscale = original.copy(upscaleMultiplier = 2)

        assertFalse(original.colorization)
        assertTrue(withColorization.colorization)
        assertTrue(withFaceEnhancement.faceEnhancement)
        assertEquals(2, withUpscale.upscaleMultiplier)
    }
}
