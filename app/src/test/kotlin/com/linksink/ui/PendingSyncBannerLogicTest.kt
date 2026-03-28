package com.linksink.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingSyncBannerLogicTest {

    @Test
    fun `shouldShowPendingSyncBanner is false when count is zero`() {
        assertFalse(shouldShowPendingSyncBanner(0))
    }

    @Test
    fun `shouldShowPendingSyncBanner is false when count is negative`() {
        assertFalse(shouldShowPendingSyncBanner(-1))
    }

    @Test
    fun `shouldShowPendingSyncBanner is true when count is positive`() {
        assertTrue(shouldShowPendingSyncBanner(1))
        assertTrue(shouldShowPendingSyncBanner(42))
    }
}
