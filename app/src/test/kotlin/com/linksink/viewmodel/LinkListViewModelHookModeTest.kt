package com.linksink.viewmodel

import com.linksink.model.HookMode
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LinkListViewModelHookModeTest {

    @Test
    fun `updateTopicHookMode to LOCAL_ONLY produces topic copy with LOCAL_ONLY and null customWebhookUrl`() {
        val topic = Topic(
            id = 1L,
            name = "Work",
            hookMode = HookMode.USE_GLOBAL,
            customWebhookUrl = null
        )
        val updated = buildUpdatedTopic(topic, HookMode.LOCAL_ONLY)
        assertEquals(HookMode.LOCAL_ONLY, updated.hookMode)
        assertNull(updated.customWebhookUrl)
        assertEquals(topic.name, updated.name)
        assertEquals(topic.id, updated.id)
    }

    @Test
    fun `updateTopicHookMode to USE_GLOBAL clears customWebhookUrl`() {
        val topic = Topic(
            id = 2L,
            name = "Personal",
            hookMode = HookMode.CUSTOM,
            customWebhookUrl = "https://discord.com/api/webhooks/123/abc"
        )
        val updated = buildUpdatedTopic(topic, HookMode.USE_GLOBAL)
        assertEquals(HookMode.USE_GLOBAL, updated.hookMode)
        assertNull(updated.customWebhookUrl)
    }

    @Test
    fun `updateTopicHookMode to CUSTOM preserves existing customWebhookUrl`() {
        val url = "https://discord.com/api/webhooks/123/abc"
        val topic = Topic(
            id = 3L,
            name = "Side Project",
            hookMode = HookMode.USE_GLOBAL,
            customWebhookUrl = url
        )
        val updated = buildUpdatedTopic(topic, HookMode.CUSTOM)
        assertEquals(HookMode.CUSTOM, updated.hookMode)
        assertEquals(url, updated.customWebhookUrl)
    }

    private fun buildUpdatedTopic(topic: Topic, mode: HookMode): Topic =
        topic.copy(
            hookMode = mode,
            customWebhookUrl = if (mode == HookMode.CUSTOM) topic.customWebhookUrl else null
        )
}
