package com.linksink.sync.providers

import com.linksink.model.Link
import com.linksink.model.SyncStatus
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscordWebhookSyncProviderTest {

    @Test
    fun `testConnection delegates to discord api`() = runBlocking {
        val api = FakeDiscordWebhookApi(
            testResult = Result.success(Unit)
        )
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = "https://discord.com/api/webhooks/1/abc"
        )

        val result = provider.testConnection(config)

        assertTrue(result.isSuccess)
        assertEquals("https://discord.com/api/webhooks/1/abc", api.lastTestUrl)
    }

    @Test
    fun `sendLink delegates to discord api and returns message id`() = runBlocking {
        val api = FakeDiscordWebhookApi(
            sendResult = Result.success("msg_123")
        )
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = "https://discord.com/api/webhooks/1/abc"
        )
        val link = Link(
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.EPOCH,
            syncStatus = SyncStatus.PENDING
        )

        val result = provider.sendLink(config, link)

        assertTrue(result.isSuccess)
        assertEquals("msg_123", result.getOrNull())
        assertEquals("https://discord.com/api/webhooks/1/abc", api.lastSendUrl)
        assertEquals(link, api.lastSendLink)
    }

    @Test
    fun `testConnection with blank url fails without calling api`() = runBlocking {
        val api = FakeDiscordWebhookApi()
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = "   "
        )

        val result = provider.testConnection(config)

        assertTrue(result.isFailure)
        assertNull(api.lastTestUrl)
    }

    @Test
    fun `sendLink with missing url fails without calling api`() = runBlocking {
        val api = FakeDiscordWebhookApi()
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = null
        )
        val link = Link(
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.EPOCH,
            syncStatus = SyncStatus.PENDING
        )

        val result = provider.sendLink(config, link)

        assertTrue(result.isFailure)
        assertNull(api.lastSendUrl)
        assertNull(api.lastSendLink)
    }

    @Test
    fun `sendLink propagates api failure`() = runBlocking {
        val api = FakeDiscordWebhookApi(
            sendResult = Result.failure(IllegalStateException("boom"))
        )
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = "https://discord.com/api/webhooks/1/abc"
        )
        val link = Link(
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.EPOCH,
            syncStatus = SyncStatus.PENDING
        )

        val result = provider.sendLink(config, link)

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun `testConnection propagates api failure`() = runBlocking {
        val api = FakeDiscordWebhookApi(
            testResult = Result.failure(IllegalStateException("nope"))
        )
        val provider = DiscordWebhookSyncProvider(api)
        val config = SyncProviderConfig(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            webhookUrl = "https://discord.com/api/webhooks/1/abc"
        )

        val result = provider.testConnection(config)

        assertTrue(result.isFailure)
        assertEquals("nope", result.exceptionOrNull()?.message)
    }
}

private class FakeDiscordWebhookApi(
    private val testResult: Result<Unit> = Result.success(Unit),
    private val sendResult: Result<String> = Result.success("id")
) : DiscordWebhookApi {
    var lastTestUrl: String? = null
        private set

    var lastSendUrl: String? = null
        private set

    var lastSendLink: Link? = null
        private set

    override suspend fun testWebhook(webhookUrl: String): Result<Unit> {
        lastTestUrl = webhookUrl
        return testResult
    }

    override suspend fun sendLink(webhookUrl: String, link: Link): Result<String> {
        lastSendUrl = webhookUrl
        lastSendLink = link
        return sendResult
    }
}

