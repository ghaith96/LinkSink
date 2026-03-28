package com.linksink.ui.theme

import androidx.compose.material3.Typography
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TypographyTest {

    @Test
    fun `LinkSinkTypography is not the default Material3 Typography`() {
        assertNotEquals(Typography(), LinkSinkTypography)
    }

    @Test
    fun `LinkSinkTypography bodyLarge differs from default`() {
        assertNotEquals(Typography().bodyLarge, LinkSinkTypography.bodyLarge)
    }

    @Test
    fun `LinkSinkTypography titleMedium differs from default`() {
        assertNotEquals(Typography().titleMedium, LinkSinkTypography.titleMedium)
    }
}
