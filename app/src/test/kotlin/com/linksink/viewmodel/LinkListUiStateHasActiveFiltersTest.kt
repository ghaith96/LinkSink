package com.linksink.viewmodel

import com.linksink.model.DateRange
import com.linksink.model.HookMode
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LinkListUiStateHasActiveFiltersTest {

    private fun link(id: Long = 1L) = Link(
        id = id,
        url = "https://example.com",
        domain = "example.com",
        topicId = null,
        savedAt = Instant.now(),
        syncStatus = SyncStatus.SYNCED
    )

    private fun topicSection() = TopicSection(
        topic = Topic(id = 1L, name = "T", hookMode = HookMode.USE_GLOBAL),
        links = listOf(link())
    )

    private fun baseSuccess() = LinkListUiState.Success(
        links = listOf(link()),
        pendingCount = 0,
        topicSections = listOf(topicSection())
    )

    @Test
    fun `hasActiveFilters is false for default success state`() {
        assertFalse(baseSuccess().hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters is true when search query is non-empty`() {
        assertTrue(baseSuccess().copy(searchQuery = "x").hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters is true when topic filter is set`() {
        assertTrue(baseSuccess().copy(topicFilter = 1L).hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters is true when date range is set`() {
        assertTrue(baseSuccess().copy(dateRange = DateRange.today()).hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters is true when link filter is not ALL`() {
        assertTrue(baseSuccess().copy(linkFilter = LinkFilter.UNREAD).hasActiveFilters)
        assertTrue(baseSuccess().copy(linkFilter = LinkFilter.ARCHIVED).hasActiveFilters)
    }
}
