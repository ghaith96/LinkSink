package com.linksink.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopBarActionsTest {

    @Test
    fun `primary top bar always exposes Search Filters and Overflow`() {
        assertEquals(
            setOf(TopBarPrimaryAction.Search, TopBarPrimaryAction.Filters, TopBarPrimaryAction.Overflow),
            linkListTopBarPrimaryActionSlots()
        )
    }

    @Test
    fun `overflow always includes Settings`() {
        val v = linkListTopBarOverflowVisibility(
            pendingCount = 0,
            archivedAvailable = false,
            notificationsAvailable = false
        )
        assertTrue(v.showSettings)
    }

    @Test
    fun `overflow shows Sync now when pending count is positive`() {
        assertTrue(
            linkListTopBarOverflowVisibility(
                pendingCount = 1,
                archivedAvailable = false,
                notificationsAvailable = false
            ).showSyncNow
        )
        assertTrue(
            linkListTopBarOverflowVisibility(
                pendingCount = 42,
                archivedAvailable = true,
                notificationsAvailable = true
            ).showSyncNow
        )
    }

    @Test
    fun `overflow hides Sync now when pending count is zero`() {
        assertFalse(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = true,
                notificationsAvailable = true
            ).showSyncNow
        )
    }

    @Test
    fun `overflow shows Archived links only when navigation is available`() {
        assertFalse(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = false,
                notificationsAvailable = false
            ).showArchivedLinks
        )
        assertTrue(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = true,
                notificationsAvailable = false
            ).showArchivedLinks
        )
    }

    @Test
    fun `overflow shows Notification settings only when navigation is available`() {
        assertFalse(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = false,
                notificationsAvailable = false
            ).showNotificationSettings
        )
        assertTrue(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = false,
                notificationsAvailable = true
            ).showNotificationSettings
        )
    }

    @Test
    fun `overflow shows Edit topics when topics are available for editing`() {
        assertTrue(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = false,
                notificationsAvailable = false,
                topicsAvailableForEdit = true
            ).showEditTopics
        )
    }

    @Test
    fun `overflow hides Edit topics when no topics exist`() {
        assertFalse(
            linkListTopBarOverflowVisibility(
                pendingCount = 0,
                archivedAvailable = true,
                notificationsAvailable = true,
                topicsAvailableForEdit = false
            ).showEditTopics
        )
    }
}
