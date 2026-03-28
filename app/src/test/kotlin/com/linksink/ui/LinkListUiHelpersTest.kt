package com.linksink.ui

import com.linksink.model.DateRange
import com.linksink.model.HookMode
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import com.linksink.viewmodel.LinkFilter
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.TopicSection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LinkListUiHelpersTest {

    private fun link(id: Long, topicId: Long? = null) = Link(
        id = id,
        url = "https://example.com/$id",
        domain = "example.com",
        topicId = topicId,
        savedAt = Instant.now(),
        syncStatus = SyncStatus.SYNCED
    )

    private fun topic(id: Long, name: String) = Topic(
        id = id, name = name, hookMode = HookMode.USE_GLOBAL
    )

    private fun successState(
        searchQuery: String = "",
        topicFilter: Long? = null,
        dateRange: DateRange? = null,
        linkFilter: LinkFilter = LinkFilter.ALL,
        sections: List<TopicSection> = emptyList()
    ) = LinkListUiState.Success(
        links = listOf(link(1L)),
        pendingCount = 0,
        searchQuery = searchQuery,
        topicFilter = topicFilter,
        dateRange = dateRange,
        linkFilter = linkFilter,
        topicSections = sections
    )

    // --- shouldShowActiveFilterChips ---

    @Test
    fun `shouldShowActiveFilterChips is false when topic and date filters are cleared`() {
        assertFalse(shouldShowActiveFilterChips(topicFilter = null, dateRange = null))
    }

    @Test
    fun `shouldShowActiveFilterChips is true when topic filter is set`() {
        assertTrue(shouldShowActiveFilterChips(topicFilter = 1L, dateRange = null))
    }

    @Test
    fun `shouldShowActiveFilterChips is true when date range is set`() {
        val range = DateRange.thisWeek()
        assertTrue(shouldShowActiveFilterChips(topicFilter = null, dateRange = range))
    }

    @Test
    fun `shouldShowActiveFilterChips is true when both topic and date are set`() {
        val range = DateRange.today()
        assertTrue(shouldShowActiveFilterChips(topicFilter = 2L, dateRange = range))
    }

    // --- useSectionedLayout ---

    @Test
    fun `useSectionedLayout is true when no active filters and sections are non-empty`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(sections = sections)
        assertTrue(useSectionedLayout(state, groupByTopic = true))
    }

    @Test
    fun `useSectionedLayout is false when groupByTopic is false even with sections`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(sections = sections)
        assertFalse(useSectionedLayout(state, groupByTopic = false))
    }

    @Test
    fun `useSectionedLayout is false when search query is active`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(searchQuery = "kotlin", sections = sections)
        assertFalse(useSectionedLayout(state, groupByTopic = true))
    }

    @Test
    fun `useSectionedLayout is false when topic filter is active`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(topicFilter = 1L, sections = sections)
        assertFalse(useSectionedLayout(state, groupByTopic = true))
    }

    @Test
    fun `useSectionedLayout is false when date range filter is active`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(dateRange = DateRange.thisWeek(), sections = sections)
        assertFalse(useSectionedLayout(state, groupByTopic = true))
    }

    @Test
    fun `useSectionedLayout is false when link filter is not ALL`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        assertFalse(useSectionedLayout(successState(linkFilter = LinkFilter.UNREAD, sections = sections), groupByTopic = true))
        assertFalse(useSectionedLayout(successState(linkFilter = LinkFilter.ARCHIVED, sections = sections), groupByTopic = true))
    }

    @Test
    fun `useSectionedLayout is false when sections list is empty`() {
        val state = successState(sections = emptyList())
        assertFalse(useSectionedLayout(state, groupByTopic = true))
    }
}
