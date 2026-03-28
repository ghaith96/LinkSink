package com.linksink.model

import java.time.Instant

data class Topic(
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val hookMode: HookMode = HookMode.USE_GLOBAL,
    val customWebhookUrl: String? = null,
    val createdAt: Instant = Instant.now(),
    val color: Int? = null,
    val emoji: String? = null,
    val displayOrder: Int = 0
) {
    init {
        require(name.isNotBlank()) { "Topic name cannot be blank" }
        require(hookMode != HookMode.CUSTOM || customWebhookUrl != null) {
            "Custom webhook URL required when hookMode is CUSTOM"
        }
    }
}

/** Returns the display name prefixed with [emoji] if present, e.g. "📌 Work". */
fun Topic.displayName(): String =
    if (emoji != null) "$emoji $name" else name
