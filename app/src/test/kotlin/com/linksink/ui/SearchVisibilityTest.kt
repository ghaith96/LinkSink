package com.linksink.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchVisibilityTest {

    @Test
    fun `shouldShowSearchField is false when query is empty and not expanded`() {
        assertFalse(shouldShowSearchField("", false))
    }

    @Test
    fun `shouldShowSearchField is true when query is empty but expanded`() {
        assertTrue(shouldShowSearchField("", true))
    }

    @Test
    fun `shouldShowSearchField is true when query is non-empty and not expanded`() {
        assertTrue(shouldShowSearchField("kotlin", false))
    }

    @Test
    fun `shouldShowSearchField is true when query is non-empty and expanded`() {
        assertTrue(shouldShowSearchField("kotlin", true))
    }

    @Test
    fun `shouldShowSearchField is true for whitespace-only query when not expanded`() {
        assertTrue(shouldShowSearchField("   ", false))
    }
}
