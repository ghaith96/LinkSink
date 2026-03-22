package com.linksink.model

import java.time.Instant

enum class SyncStatus {
    PENDING,
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
    val savedAt: Instant,
    val syncStatus: SyncStatus,
    val discordMessageId: String? = null,
    val retryCount: Int = 0
)

data class LinkMetadata(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val siteName: String?
)
