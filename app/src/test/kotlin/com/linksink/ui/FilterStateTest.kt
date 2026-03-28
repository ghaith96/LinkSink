package com.linksink.ui

import com.linksink.model.DateRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterStateTest {

    private val sampleRange = DateRange.thisWeek()

    // --- FilterState.clearAll ---

    @Test
    fun `clearAll resets topic date and groupByTopic to defaults`() {
        val dirty = FilterState(
            topicId = 42L,
            dateRange = sampleRange,
            groupByTopic = false
        )
        val cleared = dirty.clearAll()
        assertEquals(null, cleared.topicId)
        assertEquals(null, cleared.dateRange)
        assertTrue(cleared.groupByTopic)
    }

    @Test
    fun `clearAll on already cleared returns stable default shape`() {
        val cleared = FilterState.DEFAULT.clearAll()
        assertEquals(FilterState.DEFAULT, cleared)
    }

    // --- FilterState.hasPendingChanges ---

    @Test
    fun `hasPendingChanges is false when draft matches applied`() {
        val applied = FilterState(1L, sampleRange, true)
        assertFalse(applied.hasPendingChanges(applied))
    }

    @Test
    fun `hasPendingChanges is true when topic differs`() {
        val applied = FilterState(null, null, true)
        val draft = applied.copy(topicId = 9L)
        assertTrue(draft.hasPendingChanges(applied))
    }

    @Test
    fun `hasPendingChanges is true when date range differs`() {
        val applied = FilterState(null, null, true)
        val draft = applied.copy(dateRange = DateRange.today())
        assertTrue(draft.hasPendingChanges(applied))
    }

    @Test
    fun `hasPendingChanges is true when groupByTopic differs`() {
        val applied = FilterState(null, null, true)
        val draft = applied.copy(groupByTopic = false)
        assertTrue(draft.hasPendingChanges(applied))
    }

    // --- filter application: values passed to ViewModel on apply ---

    @Test
    fun `filterApplySnapshot maps state to topic and date for ViewModel`() {
        val state = FilterState(topicId = -1L, dateRange = sampleRange, groupByTopic = false)
        val snapshot = state.toApplySnapshot()
        assertEquals(-1L, snapshot.topicId)
        assertEquals(sampleRange, snapshot.dateRange)
        assertFalse(snapshot.groupByTopic)
    }

    @Test
    fun `filterApplySnapshot clears topic and date when cleared`() {
        val snapshot = FilterState.DEFAULT.toApplySnapshot()
        assertEquals(null, snapshot.topicId)
        assertEquals(null, snapshot.dateRange)
        assertTrue(snapshot.groupByTopic)
    }

    // --- filterStateFromApplied ---

    @Test
    fun `filterStateFromApplied mirrors applied topic date and grouping`() {
        val range = DateRange.today()
        val state = filterStateFromApplied(topicId = 7L, dateRange = range, groupByTopic = false)
        assertEquals(7L, state.topicId)
        assertEquals(range, state.dateRange)
        assertFalse(state.groupByTopic)
    }

    @Test
    fun `filterStateFromApplied matches default filter state when all cleared`() {
        assertEquals(FilterState.DEFAULT, filterStateFromApplied(null, null, true))
    }
}
