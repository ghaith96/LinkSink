package com.linksink.model

import com.linksink.data.local.LinkEntity
import com.linksink.data.local.toDomain
import com.linksink.data.local.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LinkTest {

    @Test
    fun `Link model includes isRead field`() {
        val link = Link(
            id = 1L,
            url = "https://example.com",
            title = "Example",
            description = null,
            thumbnailUrl = null,
            note = null,
            domain = "example.com",
            topicId = null,
            savedAt = Instant.now(),
            syncStatus = SyncStatus.LOCAL_ONLY,
            discordMessageId = null,
            retryCount = 0,
            isRead = true,
            isArchived = false
        )

        assertTrue(link.isRead)
    }

    @Test
    fun `Link model includes isArchived field`() {
        val link = Link(
            id = 1L,
            url = "https://example.com",
            title = "Example",
            description = null,
            thumbnailUrl = null,
            note = null,
            domain = "example.com",
            topicId = null,
            savedAt = Instant.now(),
            syncStatus = SyncStatus.LOCAL_ONLY,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = true
        )

        assertTrue(link.isArchived)
    }

    @Test
    fun `Link defaults isRead and isArchived to false`() {
        val link = Link(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.now(),
            syncStatus = SyncStatus.LOCAL_ONLY
        )

        assertFalse(link.isRead)
        assertFalse(link.isArchived)
    }

    @Test
    fun `toDomain converts isRead correctly`() {
        val entity = LinkEntity(
            id = 1L,
            url = "https://example.com",
            title = "Example",
            description = null,
            thumbnailUrl = null,
            note = null,
            domain = "example.com",
            topicId = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = "LOCAL_ONLY",
            discordMessageId = null,
            retryCount = 0,
            isRead = true,
            isArchived = false
        )

        val domain = entity.toDomain()
        assertTrue(domain.isRead)
    }

    @Test
    fun `toDomain converts isArchived correctly`() {
        val entity = LinkEntity(
            id = 1L,
            url = "https://example.com",
            title = "Example",
            description = null,
            thumbnailUrl = null,
            note = null,
            domain = "example.com",
            topicId = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = "LOCAL_ONLY",
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = true
        )

        val domain = entity.toDomain()
        assertTrue(domain.isArchived)
    }

    @Test
    fun `toEntity converts isRead correctly`() {
        val link = Link(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.now(),
            syncStatus = SyncStatus.LOCAL_ONLY,
            isRead = true,
            isArchived = false
        )

        val entity = link.toEntity()
        assertTrue(entity.isRead)
    }

    @Test
    fun `toEntity converts isArchived correctly`() {
        val link = Link(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.now(),
            syncStatus = SyncStatus.LOCAL_ONLY,
            isRead = false,
            isArchived = true
        )

        val entity = link.toEntity()
        assertTrue(entity.isArchived)
    }
}
