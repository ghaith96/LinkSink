package com.linksink.viewmodel

import com.linksink.model.HookMode
import com.linksink.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TopicDisplayOrderTest {

    private fun topic(id: Long, name: String, order: Int) = Topic(
        id = id, name = name, hookMode = HookMode.USE_GLOBAL,
        createdAt = Instant.ofEpochMilli(0), displayOrder = order
    )

    @Test
    fun `updateTopicOrder assigns sequential displayOrder values from 0`() {
        val topics = listOf(
            topic(3L, "Gamma", 0),
            topic(1L, "Alpha", 1),
            topic(2L, "Beta", 2)
        )
        val orderedIds = topics.map { it.id }
        val result = computeDisplayOrderUpdates(orderedIds)
        assertEquals(listOf(3L to 0, 1L to 1, 2L to 2), result)
    }

    @Test
    fun `computeDisplayOrderUpdates for single topic produces order 0`() {
        val result = computeDisplayOrderUpdates(listOf(42L))
        assertEquals(listOf(42L to 0), result)
    }

    @Test
    fun `nextDisplayOrder is maxExisting plus 1`() {
        assertEquals(3, nextDisplayOrder(maxExisting = 2))
    }

    @Test
    fun `nextDisplayOrder when no topics returns 0`() {
        assertEquals(0, nextDisplayOrder(maxExisting = null))
    }
}
