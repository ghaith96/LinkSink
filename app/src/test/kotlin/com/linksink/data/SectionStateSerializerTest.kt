package com.linksink.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionStateSerializerTest {

    @Test
    fun `encodeToJson with empty map produces empty JSON object`() {
        val json = SectionStateSerializer.encodeToJson(emptyMap())
        val decoded = SectionStateSerializer.decodeFromJson(json)
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun `encodeToJson roundtrip preserves all entries`() {
        val original = mapOf("1" to true, "2" to false, "uncategorized" to true)
        val json = SectionStateSerializer.encodeToJson(original)
        val decoded = SectionStateSerializer.decodeFromJson(json)
        assertEquals(original, decoded)
    }

    @Test
    fun `decodeFromJson with invalid JSON returns empty map`() {
        val decoded = SectionStateSerializer.decodeFromJson("not-valid-json{{{")
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun `encodeToJson with single collapsed entry roundtrips correctly`() {
        val original = mapOf("42" to false)
        val json = SectionStateSerializer.encodeToJson(original)
        val decoded = SectionStateSerializer.decodeFromJson(json)
        assertEquals(false, decoded["42"])
        assertEquals(1, decoded.size)
    }

    @Test
    fun `withSectionExpanded adds new key to existing map`() {
        val base = mapOf("1" to true)
        val updated = SectionStateSerializer.withSectionExpanded(base, "2", false)
        assertEquals(true, updated["1"])
        assertEquals(false, updated["2"])
    }

    @Test
    fun `withSectionExpanded updates existing key`() {
        val base = mapOf("1" to true)
        val updated = SectionStateSerializer.withSectionExpanded(base, "1", false)
        assertEquals(false, updated["1"])
        assertEquals(1, updated.size)
    }

    @Test
    fun `withSectionRemoved removes the given key`() {
        val base = mapOf("1" to true, "2" to false)
        val updated = SectionStateSerializer.withSectionRemoved(base, "1")
        assertEquals(1, updated.size)
        assertEquals(false, updated["2"])
    }

    @Test
    fun `withSectionRemoved on missing key returns same map`() {
        val base = mapOf("1" to true)
        val updated = SectionStateSerializer.withSectionRemoved(base, "99")
        assertEquals(base, updated)
    }

    @Test
    fun `UNCATEGORIZED_KEY constant equals the string uncategorized`() {
        assertEquals("uncategorized", SectionStateSerializer.UNCATEGORIZED_KEY)
    }
}
