package com.linksink.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TopicDisplayNameTest {

    @Test
    fun `displayName with emoji prepends emoji and space`() {
        val topic = Topic(name = "Work", emoji = "📌")
        assertEquals("📌 Work", topic.displayName())
    }

    @Test
    fun `displayName without emoji returns just the name`() {
        val topic = Topic(name = "Work", emoji = null)
        assertEquals("Work", topic.displayName())
    }

    @Test
    fun `displayName with family emoji ZWJ sequence`() {
        val topic = Topic(name = "Family", emoji = "👨‍👩‍👧")
        assertEquals("👨‍👩‍👧 Family", topic.displayName())
    }
}
