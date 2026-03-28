package com.linksink.data.local

import com.linksink.model.HookMode
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TopicEntityMapperDisplayOrderTest {

    @Test
    fun `toDomain preserves displayOrder`() {
        val entity = TopicEntity(
            id = 1,
            name = "Work",
            hookMode = HookMode.USE_GLOBAL.name,
            createdAt = 0L,
            displayOrder = 3
        )
        assertEquals(3, entity.toDomain().displayOrder)
    }

    @Test
    fun `toEntity round-trips displayOrder`() {
        val topic = Topic(name = "Inbox", createdAt = Instant.ofEpochMilli(0), displayOrder = 7)
        assertEquals(7, topic.toEntity().displayOrder)
        assertEquals(7, topic.toEntity().toDomain().displayOrder)
    }

    @Test
    fun `default displayOrder is 0`() {
        val topic = Topic(name = "Misc", createdAt = Instant.ofEpochMilli(0))
        assertEquals(0, topic.displayOrder)
        assertEquals(0, topic.toEntity().displayOrder)
    }
}
