package com.linksink.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiValidatorTest {

    @Test
    fun `null input is valid`() {
        assertTrue(isValidEmoji(null))
    }

    @Test
    fun `empty string is valid (treated as cleared)`() {
        assertTrue(isValidEmoji(""))
    }

    @Test
    fun `single basic emoji is valid`() {
        assertTrue(isValidEmoji("😀"))
    }

    @Test
    fun `single emoji with variation selector is valid`() {
        assertTrue(isValidEmoji("❤️"))
    }

    @Test
    fun `family emoji (ZWJ sequence) counts as one grapheme and is valid`() {
        assertTrue(isValidEmoji("👨‍👩‍👧"))
    }

    @Test
    fun `flag emoji (regional indicator sequence) is valid`() {
        assertTrue(isValidEmoji("🇺🇸"))
    }

    @Test
    fun `two emoji characters are invalid`() {
        assertFalse(isValidEmoji("😀😀"))
    }

    @Test
    fun `plain ASCII text is invalid`() {
        assertFalse(isValidEmoji("abc"))
    }

    @Test
    fun `single ASCII character is invalid`() {
        assertFalse(isValidEmoji("a"))
    }

    @Test
    fun `emoji followed by text is invalid`() {
        assertFalse(isValidEmoji("📌Work"))
    }
}
