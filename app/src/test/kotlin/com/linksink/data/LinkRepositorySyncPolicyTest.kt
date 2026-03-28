package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.LinkEntity
import com.linksink.data.local.TopicDao
import com.linksink.data.local.TopicEntity
import com.linksink.model.DateRange
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRepositorySyncPolicyTest {

    @Test
    fun `saveLink when sync disabled saves as LOCAL_ONLY and does not send`() = runBlocking {
        val linkDao = SyncPolicyLinkDao()
        val topicDao = SyncPolicyTopicDao()
        val discordClient = RecordingDiscordWebhookClient()
        val settings = SyncPolicySettingsStore(
            webhookUrl = null,
            syncSettings = SyncSettings(
                providerId = SyncProviderId.NONE,
                enabled = false,
                isProviderConfigValid = true
            )
        )

        val repository = LinkRepository(
            linkDao = linkDao,
            topicDao = topicDao,
            settingsStore = settings,
            metadataFetcher = SyncPolicyMetadataFetcher(),
            applicationScope = CoroutineScope(Job()),
            providerRegistry = com.linksink.sync.providers.SyncProviderRegistry(emptyList())
        )

        val result = repository.saveLink(url = "https://example.com")

        assertTrue(result.isSuccess)
        assertEquals(SyncStatus.LOCAL_ONLY, result.getOrNull()!!.syncStatus)
        assertEquals(0, discordClient.sendCalls)
    }

    @Test
    fun `saveLink when sync enabled but config invalid saves as LOCAL_ONLY and does not send`() = runBlocking {
        val linkDao = SyncPolicyLinkDao()
        val topicDao = SyncPolicyTopicDao()
        val discordClient = RecordingDiscordWebhookClient()
        val settings = SyncPolicySettingsStore(
            webhookUrl = "https://discord.com/api/webhooks/1/abc",
            syncSettings = SyncSettings(
                providerId = SyncProviderId.DISCORD_WEBHOOK,
                enabled = true,
                isProviderConfigValid = false
            )
        )

        val repository = LinkRepository(
            linkDao = linkDao,
            topicDao = topicDao,
            settingsStore = settings,
            metadataFetcher = SyncPolicyMetadataFetcher(),
            applicationScope = CoroutineScope(Job()),
            providerRegistry = com.linksink.sync.providers.SyncProviderRegistry(emptyList())
        )

        val result = repository.saveLink(url = "https://example.com")

        assertTrue(result.isSuccess)
        assertEquals(SyncStatus.LOCAL_ONLY, result.getOrNull()!!.syncStatus)
        assertEquals(0, discordClient.sendCalls)
    }
}

private class RecordingDiscordWebhookClient : com.linksink.data.remote.DiscordWebhookClient() {
    var sendCalls: Int = 0
        private set

    override suspend fun sendLink(webhookUrl: String, link: Link): Result<String> {
        sendCalls++
        return Result.success("msg_1")
    }
}

private class SyncPolicySettingsStore(
    webhookUrl: String?,
    syncSettings: SyncSettings
) : SyncSettingsStore {
    override val webhookUrl: Flow<String?> = flowOf(webhookUrl)
    override val syncSettings: Flow<SyncSettings> = flowOf(syncSettings)
}

private class SyncPolicyTopicDao : TopicDao {
    override fun getAllTopics(): Flow<List<TopicEntity>> = flowOf(emptyList())
    override suspend fun getById(id: Long): TopicEntity? = null
    override suspend fun getWithLinkCount(id: Long) = null
    override suspend fun insert(topic: TopicEntity): Long = 0
    override suspend fun update(topic: TopicEntity) = Unit
    override suspend fun delete(id: Long) = Unit
    override suspend fun unlinkAllFromTopic(topicId: Long) = Unit
    override suspend fun deleteLinksInTopic(topicId: Long) = Unit
    override fun getRecentlyUsed(limit: Int): Flow<List<TopicEntity>> = flowOf(emptyList())
    override suspend fun getByName(name: String): TopicEntity? = null
    override suspend fun getMaxDisplayOrder(): Int? = null
    override suspend fun updateDisplayOrder(id: Long, order: Int) = Unit
    override suspend fun updateDisplayOrders(updates: List<Pair<Long, Int>>) = Unit
}

private class SyncPolicyLinkDao : LinkDao {
    private val links = mutableMapOf<Long, LinkEntity>()
    private var nextId: Long = 1
    private val pendingCount = MutableStateFlow(0)

    override fun getAllLinks(): Flow<List<LinkEntity>> = flowOf(emptyList())

    override suspend fun getPendingLinks(): List<LinkEntity> = emptyList()

    override suspend fun getLinkById(id: Long): LinkEntity? = links[id]

    override suspend fun insert(link: LinkEntity): Long {
        val id = nextId++
        links[id] = link.copy(id = id)
        if (link.syncStatus == SyncStatus.PENDING.name) pendingCount.value++
        return id
    }

    override suspend fun update(link: LinkEntity) = Unit

    override suspend fun delete(link: LinkEntity) = Unit

    override suspend fun updateSyncStatus(id: Long, status: String, messageId: String?) = Unit

    override suspend fun incrementRetryCount(id: Long) = Unit

    override suspend fun markFailed(id: Long) = Unit

    override fun getPendingCount(): Flow<Int> = pendingCount

    override fun getTotalCount(): Flow<Int> = flowOf(0)

    override fun getFiltered(topicId: Long?, startDate: Long?, endDate: Long?): Flow<List<LinkEntity>> =
        flowOf(emptyList())

    override suspend fun updateTopic(linkId: Long, topicId: Long?) = Unit

    override suspend fun updateMetadata(
        linkId: Long,
        title: String?,
        description: String?,
        thumbnailUrl: String?
    ) = Unit
}

private class SyncPolicyMetadataFetcher : MetadataFetcherPort {
    override suspend fun fetch(url: String): Result<com.linksink.model.LinkMetadata> =
        Result.failure(IllegalStateException("disabled in unit tests"))
}

