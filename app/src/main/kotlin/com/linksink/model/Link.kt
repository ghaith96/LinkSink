package com.linksink.model

import java.time.Instant

enum class SyncStatus {
    PENDING,
    LOCAL_ONLY,
    SYNCED,
    FAILED
}

data class Link(
    val id: Long = 0,
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val note: String? = null,
    val domain: String,
    val topicId: Long? = null,
    val savedAt: Instant,
    val syncStatus: SyncStatus,
    val discordMessageId: String? = null,
    val retryCount: Int = 0,
    val isRead: Boolean = false,
    val isArchived: Boolean = false
)

data class LinkMetadata(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val siteName: String?
) {
    companion object {
        val EMPTY = LinkMetadata(null, null, null, null)
    }

    val isEmpty: Boolean get() = title == null && description == null && imageUrl == null
}
