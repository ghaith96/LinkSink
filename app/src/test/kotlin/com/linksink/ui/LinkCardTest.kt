package com.linksink.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkCardTest {

    @Test
    fun `faviconUrl returns Google S2 favicon URL for a domain`() {
        val url = faviconUrl("github.com")
        assertEquals("https://www.google.com/s2/favicons?sz=64&domain=github.com", url)
    }

    @Test
    fun `faviconUrl works for subdomains`() {
        val url = faviconUrl("docs.example.com")
        assertTrue(url.contains("domain=docs.example.com"))
    }

    @Test
    fun `faviconUrl always includes sz=64`() {
        val url = faviconUrl("example.com")
        assertTrue(url.contains("sz=64"))
    }
}
