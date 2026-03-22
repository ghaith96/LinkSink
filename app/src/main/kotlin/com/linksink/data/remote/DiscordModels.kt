package com.linksink.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookPayload(
    val content: String? = null,
    val embeds: List<DiscordEmbed>
)

@Serializable
data class DiscordEmbed(
    val title: String? = null,
    val url: String,
    val description: String? = null,
    val color: Int = 0x58ACFA,
    val thumbnail: DiscordThumbnail? = null,
    val footer: DiscordFooter = DiscordFooter("Saved via LinkSink"),
    val timestamp: String
)

@Serializable
data class DiscordThumbnail(
    val url: String
)

@Serializable
data class DiscordFooter(
    val text: String
)

@Serializable
data class DiscordWebhookResponse(
    val id: String
)

@Serializable
data class DiscordTestPayload(
    val content: String,
    @SerialName("username")
    val username: String = "LinkSink"
)
