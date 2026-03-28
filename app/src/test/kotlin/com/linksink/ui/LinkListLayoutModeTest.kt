package com.linksink.ui

import com.linksink.model.HookMode
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import com.linksink.ui.useSectionedLayout
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.TopicSection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LinkListLayoutModeTest {

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
        sections: List<TopicSection> = emptyList()
    ) = LinkListUiState.Success(
        links = listOf(link(1L)),
        pendingCount = 0,
        searchQuery = searchQuery,
        topicFilter = topicFilter,
        topicSections = sections
    )

    @Test
    fun `useSectionedLayout is true when no active filters and sections are non-empty`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(sections = sections)
        assertTrue(useSectionedLayout(state))
    }

    @Test
    fun `useSectionedLayout is false when search query is active`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(searchQuery = "kotlin", sections = sections)
        assertFalse(useSectionedLayout(state))
    }

    @Test
    fun `useSectionedLayout is false when topic filter is active`() {
        val sections = listOf(TopicSection(topic = topic(1L, "Work"), links = listOf(link(1L))))
        val state = successState(topicFilter = 1L, sections = sections)
        assertFalse(useSectionedLayout(state))
    }

    @Test
    fun `useSectionedLayout is false when sections list is empty`() {
        val state = successState(sections = emptyList())
        assertFalse(useSectionedLayout(state))
    }
}
