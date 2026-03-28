package com.linksink.sync.providers

import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscordWebhookClientApiTest {

    @Test
    fun `api delegates to DiscordWebhookClient`() = runBlocking {
        val client = RecordingDiscordWebhookClient(
            testResult = Result.success(Unit),
            sendResult = Result.success("msg_1")
        )
        val api = DiscordWebhookClientApi(client)
        val link = Link(
            url = "https://example.com",
            domain = "example.com",
            savedAt = Instant.EPOCH,
            syncStatus = SyncStatus.PENDING
        )

        val test = api.testWebhook("https://discord.com/api/webhooks/1/abc")
        val send = api.sendLink("https://discord.com/api/webhooks/1/abc", link)
        api.close()

        assertTrue(test.isSuccess)
        assertTrue(send.isSuccess)
        assertEquals("https://discord.com/api/webhooks/1/abc", client.lastTestUrl)
        assertEquals("https://discord.com/api/webhooks/1/abc", client.lastSendUrl)
        assertEquals(link, client.lastSendLink)
        assertEquals(1, client.closeCalls)
    }
}

private class RecordingDiscordWebhookClient(
    private val testResult: Result<Unit>,
    private val sendResult: Result<String>
) : DiscordWebhookClient() {
    var lastTestUrl: String? = null
        private set
    var lastSendUrl: String? = null
        private set
    var lastSendLink: Link? = null
        private set
    var closeCalls: Int = 0
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

    override fun close() {
        closeCalls++
    }
}

