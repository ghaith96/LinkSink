package com.linksink.ui

import com.linksink.model.Link
import com.linksink.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class LinkStatusUiModelTest {

    private fun baseLink(
        isRead: Boolean = false,
        isArchived: Boolean = false
    ) = Link(
        id = 1L,
        url = "https://example.com",
        domain = "example.com",
        savedAt = Instant.parse("2026-01-01T12:00:00Z"),
        syncStatus = SyncStatus.SYNCED,
        isRead = isRead,
        isArchived = isArchived
    )

    @Test
    fun `linkStatusFromLink maps archived over read state`() {
        assertEquals(LinkStatus.ARCHIVED, linkStatusFromLink(baseLink(isRead = true, isArchived = true)))
        assertEquals(LinkStatus.ARCHIVED, linkStatusFromLink(baseLink(isRead = false, isArchived = true)))
    }

    @Test
    fun `linkStatusFromLink maps unread when not archived and not read`() {
        assertEquals(LinkStatus.UNREAD, linkStatusFromLink(baseLink(isRead = false, isArchived = false)))
    }

    @Test
    fun `linkStatusFromLink maps read when not archived and read`() {
        assertEquals(LinkStatus.READ, linkStatusFromLink(baseLink(isRead = true, isArchived = false)))
    }

    @Test
    fun `fromLinkStatus UNREAD returns unread dot icon high emphasis and unread accent`() {
        val m = LinkStatusUiModel.fromLinkStatus(LinkStatus.UNREAD)
        assertEquals(LinkStatusIndicatorIcon.UNREAD_DOT, m.icon)
        assertEquals(LinkStatusEmphasis.HIGH, m.emphasis)
        assertEquals(LinkStatusAccentStripe.UNREAD, m.accentStripe)
        assertEquals("Unread link", m.contentDescription)
    }

    @Test
    fun `fromLinkStatus READ returns check icon standard emphasis`() {
        val m = LinkStatusUiModel.fromLinkStatus(LinkStatus.READ)
        assertEquals(LinkStatusIndicatorIcon.READ_CHECK, m.icon)
        assertEquals(LinkStatusEmphasis.STANDARD, m.emphasis)
        assertEquals(LinkStatusAccentStripe.READ, m.accentStripe)
        assertEquals("Read link", m.contentDescription)
    }

    @Test
    fun `fromLinkStatus ARCHIVED returns archive icon subdued emphasis`() {
        val m = LinkStatusUiModel.fromLinkStatus(LinkStatus.ARCHIVED)
        assertEquals(LinkStatusIndicatorIcon.ARCHIVE, m.icon)
        assertEquals(LinkStatusEmphasis.SUBDUED, m.emphasis)
        assertEquals(LinkStatusAccentStripe.ARCHIVED, m.accentStripe)
        assertEquals("Archived link", m.contentDescription)
    }
}
