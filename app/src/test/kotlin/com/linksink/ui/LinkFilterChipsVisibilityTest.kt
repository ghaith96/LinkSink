package com.linksink.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkFilterChipsVisibilityTest {

    @Test
    fun `shouldShowLinkFilterChips is false when no links exist`() {
        assertFalse(shouldShowLinkFilterChips(hasLinks = false, hasReadLinks = false))
    }

    @Test
    fun `shouldShowLinkFilterChips is false when only unread links exist`() {
        assertFalse(shouldShowLinkFilterChips(hasLinks = true, hasReadLinks = false))
    }

    @Test
    fun `shouldShowLinkFilterChips is true when only read links exist`() {
        assertTrue(shouldShowLinkFilterChips(hasLinks = true, hasReadLinks = true))
    }

    @Test
    fun `shouldShowLinkFilterChips is true when both read and unread links exist`() {
        // When both read and unread exist, we have both hasLinks=true and hasReadLinks=true
        // This is the same as "only read links" case, but semantically means filtering is useful
        assertTrue(shouldShowLinkFilterChips(hasLinks = true, hasReadLinks = true))
    }
}
