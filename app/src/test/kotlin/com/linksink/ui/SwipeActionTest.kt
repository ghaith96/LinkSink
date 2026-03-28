package com.linksink.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeActionTest {

    @Test
    fun `getSwipeAction when not archived returns archive label and icon`() {
        val a = getSwipeAction(isArchived = false)
        assertEquals("Archive", a.label)
        assertEquals("Archive", a.contentDescription)
        assertEquals(SwipeStartToEndIconKind.ARCHIVE, a.iconKind)
        assertEquals(SwipeStartToEndColorRole.TERTIARY_CONTAINER, a.colorRole)
    }

    @Test
    fun `getSwipeAction when archived returns unarchive label and icon`() {
        val a = getSwipeAction(isArchived = true)
        assertEquals("Unarchive", a.label)
        assertEquals("Unarchive", a.contentDescription)
        assertEquals(SwipeStartToEndIconKind.UNARCHIVE, a.iconKind)
        assertEquals(SwipeStartToEndColorRole.SECONDARY_CONTAINER, a.colorRole)
    }
}
