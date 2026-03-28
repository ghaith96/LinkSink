package com.linksink.viewmodel

import com.linksink.model.HookMode
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TopicViewModelCreateTest {

    private fun buildTopic(
        name: String,
        hookMode: HookMode = HookMode.USE_GLOBAL,
        customUrl: String? = null,
        color: Int? = null,
        emoji: String? = null
    ) = Topic(
        name = name,
        hookMode = hookMode,
        customWebhookUrl = if (hookMode == HookMode.CUSTOM) customUrl else null,
        color = color,
        emoji = emoji
    )

    @Test
    fun `topic built with color retains that color`() {
        val topic = buildTopic(name = "Work", color = 0xFF5865F2.toInt())
        assertEquals(0xFF5865F2.toInt(), topic.color)
    }

    @Test
    fun `topic built with emoji retains that emoji`() {
        val topic = buildTopic(name = "Reading", emoji = "📚")
        assertEquals("📚", topic.emoji)
    }

    @Test
    fun `topic built with null color and emoji has both null`() {
        val topic = buildTopic(name = "Inbox")
        assertNull(topic.color)
        assertNull(topic.emoji)
    }

    @Test
    fun `custom webhook url only set when hookMode is CUSTOM`() {
        val custom = buildTopic("A", HookMode.CUSTOM, "https://example.com")
        assertEquals("https://example.com", custom.customWebhookUrl)

        val global = buildTopic("B", HookMode.USE_GLOBAL, "https://example.com")
        assertNull(global.customWebhookUrl)
    }
}
