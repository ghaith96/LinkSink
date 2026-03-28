package com.linksink.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

object SectionStateSerializer {

    const val UNCATEGORIZED_KEY = "uncategorized"

    private val json = Json { ignoreUnknownKeys = true }

    fun encodeToJson(states: Map<String, Boolean>): String =
        json.encodeToString(states)

    fun decodeFromJson(raw: String): Map<String, Boolean> =
        try {
            val obj = json.parseToJsonElement(raw) as? JsonObject ?: return emptyMap()
            obj.entries.associate { (k, v) -> k to v.jsonPrimitive.boolean }
        } catch (_: Exception) {
            emptyMap()
        }

    fun withSectionExpanded(
        current: Map<String, Boolean>,
        key: String,
        expanded: Boolean
    ): Map<String, Boolean> = current + (key to expanded)

    fun withSectionRemoved(
        current: Map<String, Boolean>,
        key: String
    ): Map<String, Boolean> = current - key
}
