package com.linksink.viewmodel

import com.linksink.model.HookMode
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GroupLinksByTopicTest {

    private fun link(id: Long, topicId: Long?) = Link(
        id = id,
        url = "https://example.com/$id",
        domain = "example.com",
        topicId = topicId,
        savedAt = Instant.now(),
        syncStatus = SyncStatus.SYNCED
    )

    private fun topic(id: Long, name: String) = Topic(
        id = id,
        name = name,
        hookMode = HookMode.USE_GLOBAL
    )

    @Test
    fun `groups links by topic with named sections sorted alphabetically`() {
        val work = topic(1L, "Work")
        val personal = topic(2L, "Personal")
        val links = listOf(
            link(1L, 1L),
            link(2L, 2L),
            link(3L, 1L)
        )
        val sections = groupLinksByTopic(links, listOf(work, personal))

        assertEquals(2, sections.size)
        // alphabetical: Personal first, then Work
        assertEquals("Personal", sections[0].topic?.name)
        assertEquals(1, sections[0].links.size)
        assertEquals("Work", sections[1].topic?.name)
        assertEquals(2, sections[1].links.size)
    }

    @Test
    fun `uncategorized links appear in last section with null topic`() {
        val work = topic(1L, "Work")
        val links = listOf(
            link(1L, 1L),
            link(2L, null)
        )
        val sections = groupLinksByTopic(links, listOf(work))

        assertEquals(2, sections.size)
        assertEquals("Work", sections[0].topic?.name)
        assertNull(sections[1].topic)
        assertEquals(1, sections[1].links.size)
        assertEquals(2L, sections[1].links.single().id)
    }

    @Test
    fun `topics with zero links are excluded`() {
        val work = topic(1L, "Work")
        val empty = topic(2L, "Empty")
        val links = listOf(link(1L, 1L))

        val sections = groupLinksByTopic(links, listOf(work, empty))

        assertEquals(1, sections.size)
        assertEquals("Work", sections[0].topic?.name)
    }

    @Test
    fun `returns empty list when no links`() {
        val sections = groupLinksByTopic(emptyList(), listOf(topic(1L, "Work")))
        assertTrue(sections.isEmpty())
    }

    @Test
    fun `only uncategorized links produces single null-topic section`() {
        val links = listOf(link(1L, null), link(2L, null))
        val sections = groupLinksByTopic(links, emptyList())

        assertEquals(1, sections.size)
        assertNull(sections[0].topic)
        assertEquals(2, sections[0].links.size)
    }

    @Test
    fun `links with unknown topicId (topic deleted) go to uncategorized`() {
        val links = listOf(link(1L, 99L))
        val sections = groupLinksByTopic(links, emptyList())

        assertEquals(1, sections.size)
        assertNull(sections[0].topic)
    }
}
