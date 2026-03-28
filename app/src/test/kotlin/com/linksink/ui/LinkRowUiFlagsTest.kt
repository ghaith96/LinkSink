package com.linksink.ui

import com.linksink.model.SyncStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRowUiFlagsTest {

    // --- sync icon: omit noise for SYNCED ---

    @Test
    fun `sync status icon hidden for SYNCED`() {
        assertFalse(LinkRowUiFlags.shouldShowSyncStatusIcon(SyncStatus.SYNCED))
    }

    @Test
    fun `sync status icon shown for PENDING`() {
        assertTrue(LinkRowUiFlags.shouldShowSyncStatusIcon(SyncStatus.PENDING))
    }

    @Test
    fun `sync status icon shown for FAILED`() {
        assertTrue(LinkRowUiFlags.shouldShowSyncStatusIcon(SyncStatus.FAILED))
    }

    @Test
    fun `sync status icon shown for LOCAL_ONLY`() {
        assertTrue(LinkRowUiFlags.shouldShowSyncStatusIcon(SyncStatus.LOCAL_ONLY))
    }
}
