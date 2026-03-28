package com.linksink.data.local

import com.linksink.model.HookMode
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TopicEntityMapperTest {

    @Test
    fun `toDomain preserves emoji when set`() {
        val entity = TopicEntity(
            id = 1,
            name = "Work",
            hookMode = HookMode.USE_GLOBAL.name,
            createdAt = 0L,
            emoji = "📌"
        )
        val domain = entity.toDomain()
        assertEquals("📌", domain.emoji)
    }

    @Test
    fun `toDomain maps null emoji correctly`() {
        val entity = TopicEntity(
            id = 2,
            name = "Personal",
            hookMode = HookMode.USE_GLOBAL.name,
            createdAt = 0L,
            emoji = null
        )
        val domain = entity.toDomain()
        assertNull(domain.emoji)
    }

    @Test
    fun `toEntity round-trips emoji`() {
        val topic = Topic(
            id = 3,
            name = "Reading",
            createdAt = Instant.ofEpochMilli(0),
            emoji = "📚"
        )
        val entity = topic.toEntity()
        assertEquals("📚", entity.emoji)
        assertEquals("📚", entity.toDomain().emoji)
    }

    @Test
    fun `toEntity round-trips null emoji`() {
        val topic = Topic(
            id = 4,
            name = "Misc",
            createdAt = Instant.ofEpochMilli(0),
            emoji = null
        )
        val entity = topic.toEntity()
        assertNull(entity.emoji)
        assertNull(entity.toDomain().emoji)
    }
}
