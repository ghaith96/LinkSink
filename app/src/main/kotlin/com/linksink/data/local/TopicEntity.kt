package com.linksink.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.linksink.model.HookMode
import com.linksink.model.Topic
import java.time.Instant

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,

    @ColumnInfo(name = "hook_mode")
    val hookMode: String = HookMode.USE_GLOBAL.name,

    @ColumnInfo(name = "custom_webhook_url")
    val customWebhookUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "color")
    val color: Int? = null
)

fun TopicEntity.toDomain(): Topic = Topic(
    id = id,
    name = name,
    parentId = parentId,
    hookMode = HookMode.valueOf(hookMode),
    customWebhookUrl = customWebhookUrl,
    createdAt = Instant.ofEpochMilli(createdAt),
    color = color
)

fun Topic.toEntity(): TopicEntity = TopicEntity(
    id = id,
    name = name,
    parentId = parentId,
    hookMode = hookMode.name,
    customWebhookUrl = customWebhookUrl,
    createdAt = createdAt.toEpochMilli(),
    color = color
)

data class TopicWithCount(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val hookMode: String,
    val customWebhookUrl: String?,
    val createdAt: Long,
    val color: Int?,
    val linkCount: Int
)
