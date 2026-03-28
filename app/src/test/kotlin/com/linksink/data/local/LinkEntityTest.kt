package com.linksink.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LinkEntityTest {

    @Test
    fun `LinkEntity can be instantiated with isRead field`() {
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

        assertEquals(true, entity.isRead)
    }

    @Test
    fun `LinkEntity can be instantiated with isArchived field`() {
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

        assertEquals(true, entity.isArchived)
    }

    @Test
    fun `LinkEntity defaults isRead and isArchived to false`() {
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
            retryCount = 0
        )

        assertFalse(entity.isRead)
        assertFalse(entity.isArchived)
    }
}
