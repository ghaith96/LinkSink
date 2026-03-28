package com.linksink.viewmodel

import com.linksink.data.SectionStateSerializer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicViewModelDeleteCleanupTest {

    @Test
    fun `withSectionRemoved clears the deleted topic key`() {
        val states = mapOf("1" to false, "2" to true, "uncategorized" to true)
        val updated = SectionStateSerializer.withSectionRemoved(states, "1")
        assertFalse(updated.containsKey("1"))
        assertTrue(updated.containsKey("2"))
        assertTrue(updated.containsKey("uncategorized"))
    }

    @Test
    fun `withSectionRemoved on unknown key is a no-op`() {
        val states = mapOf("2" to true)
        val updated = SectionStateSerializer.withSectionRemoved(states, "99")
        assertTrue(updated.containsKey("2"))
        assertFalse(updated.containsKey("99"))
    }
}
