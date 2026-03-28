package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.LinkEntity
import com.linksink.data.local.TopicDao
import com.linksink.data.local.TopicEntity
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.sync.providers.SyncProvider
import com.linksink.sync.providers.SyncProviderConfig
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.providers.SyncProviderRegistry
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

class LinkRepositoryProviderSyncTest {

    @Test
    fun `syncPendingLinks uses provider and updates sync status on success`() = runBlocking {
        val linkDao = RecordingLinkDao(
            pendingLinks = listOf(
                LinkEntity(
                    id = 1,
                    url = "https://example.com",
                    title = null,
                    description = null,
                    thumbnailUrl = null,
                    note = null,
                    domain = "example.com",
                    topicId = null,
                    savedAt = 0L,
                    syncStatus = SyncStatus.PENDING.name,
                    discordMessageId = null,
                    retryCount = 0
                )
            )
        )
        val topicDao = ProviderSyncTopicDao()
        val provider = RecordingProvider(
            id = SyncProviderId.DISCORD_WEBHOOK,
            sendResult = Result.success("msg_1")
        )
        val registry = SyncProviderRegistry(
            providers = listOf(provider)
        )
        val settings = ProviderSyncSettingsStore(
            webhookUrl = "https://discord.com/api/webhooks/1/abc",
            syncSettings = SyncSettings(
                providerId = SyncProviderId.DISCORD_WEBHOOK,
                enabled = true,
                isProviderConfigValid = true
            )
        )

        val repository = LinkRepository(
            linkDao = linkDao,
            topicDao = topicDao,
            settingsStore = settings,
            metadataFetcher = ProviderSyncMetadataFetcher(),
            applicationScope = CoroutineScope(Job()),
            providerRegistry = registry
        )

        val result = repository.syncPendingLinks()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        assertEquals(1, provider.sendCalls)
        assertEquals("https://discord.com/api/webhooks/1/abc", provider.lastConfig?.webhookUrl)
        assertEquals(1, linkDao.updateSyncStatusCalls)
        assertEquals(SyncStatus.SYNCED.name, linkDao.lastUpdatedStatus)
        assertEquals("msg_1", linkDao.lastUpdatedMessageId)
    }

    @Test
    fun `syncPendingLinks does nothing when provider config invalid`() = runBlocking {
        val linkDao = RecordingLinkDao(
            pendingLinks = listOf(
                LinkEntity(
                    id = 1,
                    url = "https://example.com",
                    title = null,
                    description = null,
                    thumbnailUrl = null,
                    note = null,
                    domain = "example.com",
                    topicId = null,
                    savedAt = 0L,
                    syncStatus = SyncStatus.PENDING.name,
                    discordMessageId = null,
                    retryCount = 0
                )
            )
        )
        val provider = RecordingProvider(
            id = SyncProviderId.DISCORD_WEBHOOK,
            sendResult = Result.success("msg_1")
        )
        val registry = SyncProviderRegistry(
            providers = listOf(provider)
        )
        val settings = ProviderSyncSettingsStore(
            webhookUrl = "https://discord.com/api/webhooks/1/abc",
            syncSettings = SyncSettings(
                providerId = SyncProviderId.DISCORD_WEBHOOK,
                enabled = true,
                isProviderConfigValid = false
            )
        )

        val repository = LinkRepository(
            linkDao = linkDao,
            topicDao = ProviderSyncTopicDao(),
            settingsStore = settings,
            metadataFetcher = ProviderSyncMetadataFetcher(),
            applicationScope = CoroutineScope(Job()),
            providerRegistry = registry
        )

        val result = repository.syncPendingLinks()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
        assertEquals(0, provider.sendCalls)
        assertEquals(0, linkDao.updateSyncStatusCalls)
    }
}

private class RecordingProvider(
    override val id: String,
    private val sendResult: Result<String?>
) : SyncProvider {
    var sendCalls: Int = 0
        private set
    var lastConfig: SyncProviderConfig? = null
        private set
    var lastLink: Link? = null
        private set

    override suspend fun testConnection(config: SyncProviderConfig): Result<Unit> =
        Result.success(Unit)

    override suspend fun sendLink(config: SyncProviderConfig, link: Link): Result<String?> {
        sendCalls++
        lastConfig = config
        lastLink = link
        return sendResult
    }
}

private class RecordingLinkDao(
    private val pendingLinks: List<LinkEntity>
) : LinkDao {
    var updateSyncStatusCalls: Int = 0
        private set
    var lastUpdatedStatus: String? = null
        private set
    var lastUpdatedMessageId: String? = null
        private set

    override fun getAllLinks(): Flow<List<LinkEntity>> = flowOf(emptyList())
    override suspend fun getPendingLinks(): List<LinkEntity> = pendingLinks
    override suspend fun getLinkById(id: Long): LinkEntity? = pendingLinks.firstOrNull { it.id == id }
    override suspend fun insert(link: LinkEntity): Long = 0
    override suspend fun update(link: LinkEntity) = Unit
    override suspend fun delete(link: LinkEntity) = Unit

    override suspend fun updateSyncStatus(id: Long, status: String, messageId: String?) {
        updateSyncStatusCalls++
        lastUpdatedStatus = status
        lastUpdatedMessageId = messageId
    }

    override suspend fun incrementRetryCount(id: Long) = Unit
    override suspend fun markFailed(id: Long) = Unit
    override fun getPendingCount(): Flow<Int> = MutableStateFlow(pendingLinks.size)
    override fun getTotalCount(): Flow<Int> = flowOf(0)
    override fun getFiltered(topicId: Long?, startDate: Long?, endDate: Long?): Flow<List<LinkEntity>> = flowOf(emptyList())
    override suspend fun updateTopic(linkId: Long, topicId: Long?) = Unit
    override suspend fun updateMetadata(linkId: Long, title: String?, description: String?, thumbnailUrl: String?) = Unit
}

private class ProviderSyncTopicDao : TopicDao {
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

private class ProviderSyncMetadataFetcher : MetadataFetcherPort {
    override suspend fun fetch(url: String): Result<com.linksink.model.LinkMetadata> =
        Result.failure(IllegalStateException("disabled in unit tests"))
}

private class ProviderSyncSettingsStore(
    webhookUrl: String?,
    syncSettings: SyncSettings
) : SyncSettingsStore {
    override val webhookUrl: Flow<String?> = flowOf(webhookUrl)
    override val syncSettings: Flow<SyncSettings> = flowOf(syncSettings)
}

