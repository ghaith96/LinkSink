package com.linksink.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import java.time.Instant

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "domain")
    val domain: String,

    @ColumnInfo(name = "topic_id")
    val topicId: Long? = null,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String,

    @ColumnInfo(name = "discord_message_id")
    val discordMessageId: String?,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)

fun LinkEntity.toDomain(): Link = Link(
    id = id,
    url = url,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    note = note,
    domain = domain,
    topicId = topicId,
    savedAt = Instant.ofEpochMilli(savedAt),
    syncStatus = SyncStatus.valueOf(syncStatus),
    discordMessageId = discordMessageId,
    retryCount = retryCount,
    isRead = isRead,
    isArchived = isArchived
)

fun Link.toEntity(): LinkEntity = LinkEntity(
    id = id,
    url = url,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    note = note,
    domain = domain,
    topicId = topicId,
    savedAt = savedAt.toEpochMilli(),
    syncStatus = syncStatus.name,
    discordMessageId = discordMessageId,
    retryCount = retryCount,
    isRead = isRead,
    isArchived = isArchived
)
