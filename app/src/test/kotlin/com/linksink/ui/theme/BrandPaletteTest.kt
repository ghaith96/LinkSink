package com.linksink.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BrandPaletteTest {

    @Test
    fun `LightColorScheme primary is LinkSink brand blue`() {
        assertEquals(LinkSinkPrimary, LightColorScheme.primary)
    }

    @Test
    fun `DarkColorScheme primary is LinkSink brand blue light variant`() {
        assertEquals(LinkSinkPrimaryLight, DarkColorScheme.primary)
    }

    @Test
    fun `LightColorScheme secondary is not the template purple`() {
        val templatePurple = Color(0xFF625b71)
        assertNotEquals(templatePurple, LightColorScheme.secondary)
    }

    @Test
    fun `DarkColorScheme secondary is not the template purple grey`() {
        val templatePurpleGrey80 = Color(0xFFCCC2DC)
        assertNotEquals(templatePurpleGrey80, DarkColorScheme.secondary)
    }

    @Test
    fun `LightColorScheme tertiary is not the template pink`() {
        val templatePink40 = Color(0xFF7D5260)
        assertNotEquals(templatePink40, LightColorScheme.tertiary)
    }

    @Test
    fun `DarkColorScheme tertiary is not the template pink`() {
        val templatePink80 = Color(0xFFEFB8C8)
        assertNotEquals(templatePink80, DarkColorScheme.tertiary)
    }
}
