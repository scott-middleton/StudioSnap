package com.middleton.studiosnap.core.data.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageResizerTest {

    @Test
    fun `square image matches the square bucket`() {
        assertEquals(1024 to 1024, closestKontextResolution(3000, 3000))
    }

    @Test
    fun `3 by 4 portrait matches the closest portrait bucket`() {
        assertEquals(880 to 1184, closestKontextResolution(3000, 4000))
    }

    @Test
    fun `4 by 3 landscape matches the closest landscape bucket`() {
        assertEquals(1184 to 880, closestKontextResolution(4000, 3000))
    }

    @Test
    fun `extreme tall aspect ratio matches the tallest bucket`() {
        assertEquals(672 to 1568, closestKontextResolution(100, 1000))
    }

    @Test
    fun `extreme wide aspect ratio matches the widest bucket`() {
        assertEquals(1568 to 672, closestKontextResolution(1000, 100))
    }

    @Test
    fun `small image still picks a bucket by aspect ratio, upscaling is left to resizeImage caller policy`() {
        assertEquals(1024 to 1024, closestKontextResolution(300, 300))
    }
}
