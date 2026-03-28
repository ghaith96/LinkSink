package com.linksink.ui

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

class LinkListUiStateStatsTest {

    private fun link(id: Long, isRead: Boolean = false, isArchived: Boolean = false) = Link(
        id = id,
        url = "https://example.com/$id",
        domain = "example.com",
        topicId = null,
        savedAt = Instant.now(),
        syncStatus = SyncStatus.SYNCED,
        isRead = isRead,
        isArchived = isArchived
    )

    private fun successState(
        links: List<Link> = emptyList(),
        linkFilter: LinkFilter = LinkFilter.ALL
    ) = LinkListUiState.Success(
        links = links,
        pendingCount = 0,
        linkFilter = linkFilter,
        topicSections = emptyList()
    )

    // --- hasLinks ---

    @Test
    fun `hasLinks is false when links list is empty`() {
        assertFalse(successState(links = emptyList()).hasLinks)
    }

    @Test
    fun `hasLinks is true when links list has unread items`() {
        assertTrue(successState(links = listOf(link(1L, isRead = false))).hasLinks)
    }

    @Test
    fun `hasLinks is true when links list has read items`() {
        assertTrue(successState(links = listOf(link(1L, isRead = true))).hasLinks)
    }

    @Test
    fun `hasLinks is true when links list has mix of read and unread`() {
        assertTrue(
            successState(
                links = listOf(
                    link(1L, isRead = false),
                    link(2L, isRead = true)
                )
            ).hasLinks
        )
    }

    // --- hasReadLinks ---

    @Test
    fun `hasReadLinks is false when all links are unread`() {
        assertFalse(
            successState(
                links = listOf(
                    link(1L, isRead = false),
                    link(2L, isRead = false)
                )
            ).hasReadLinks
        )
    }

    @Test
    fun `hasReadLinks is true when at least one link is read`() {
        assertTrue(
            successState(
                links = listOf(
                    link(1L, isRead = false),
                    link(2L, isRead = true)
                )
            ).hasReadLinks
        )
    }

    @Test
    fun `hasReadLinks is true when all links are read`() {
        assertTrue(
            successState(
                links = listOf(
                    link(1L, isRead = true),
                    link(2L, isRead = true)
                )
            ).hasReadLinks
        )
    }

    @Test
    fun `hasReadLinks is false when links list is empty`() {
        assertFalse(successState(links = emptyList()).hasReadLinks)
    }
}
