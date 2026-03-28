package com.linksink.sync.providers

import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.Link

interface DiscordWebhookApi {
    suspend fun testWebhook(webhookUrl: String): Result<Unit>
    suspend fun sendLink(webhookUrl: String, link: Link): Result<String>
    fun close() = Unit
}

class DiscordWebhookClientApi(
    private val client: DiscordWebhookClient
) : DiscordWebhookApi {
    override suspend fun testWebhook(webhookUrl: String): Result<Unit> =
        client.testWebhook(webhookUrl)

    override suspend fun sendLink(webhookUrl: String, link: Link): Result<String> =
        client.sendLink(webhookUrl, link)

    override fun close() = client.close()
}

class DiscordWebhookSyncProvider(
    private val api: DiscordWebhookApi
) : SyncProvider {
    override val id: String = SyncProviderId.DISCORD_WEBHOOK

    override suspend fun testConnection(config: SyncProviderConfig): Result<Unit> {
        val url = config.webhookUrl?.trim().orEmpty()
        if (url.isBlank()) return Result.failure(IllegalArgumentException("Missing webhook URL"))
        return api.testWebhook(url)
    }

    override suspend fun sendLink(config: SyncProviderConfig, link: Link): Result<String?> {
        val url = config.webhookUrl?.trim().orEmpty()
        if (url.isBlank()) return Result.failure(IllegalArgumentException("Missing webhook URL"))
        return api.sendLink(url, link).map { it }
    }

    override fun close() = api.close()
}

