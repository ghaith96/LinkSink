package com.linksink.ui.components

import com.linksink.data.SectionStateSerializer
import com.linksink.model.HookMode
import com.linksink.model.Topic
import com.linksink.viewmodel.TopicSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TopicSectionHeaderTest {

    private fun topic(id: Long, name: String, hookMode: HookMode = HookMode.USE_GLOBAL) =
        Topic(id = id, name = name, hookMode = hookMode)

    @Test
    fun `TopicSection sectionKey is topic id string for named topics`() {
        val section = TopicSection(topic = topic(42L, "Work"), links = emptyList())
        assertEquals("42", section.sectionKey)
    }

    @Test
    fun `TopicSection sectionKey is uncategorized constant for null topic`() {
        val section = TopicSection(topic = null, links = emptyList())
        assertEquals(SectionStateSerializer.UNCATEGORIZED_KEY, section.sectionKey)
    }

    @Test
    fun `TopicSection with null topic has no topic`() {
        val section = TopicSection(topic = null, links = emptyList())
        assertNull(section.topic)
    }
}
