package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.LinkEntity
import com.linksink.data.local.TopicDao
import com.linksink.data.local.TopicEntity
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PendingCountGatingTest {

    @Test
    fun `pending count is zero when sync disabled even if dao has pending rows`() = runBlocking {
        val linkDao = PendingCountLinkDao(pendingCount = 7)
        val topicDao = EmptyTopicDao()
        val settings = PendingCountSettingsStore(
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
            metadataFetcher = NoopMetadataFetcher(),
            applicationScope = CoroutineScope(Job()),
            providerRegistry = com.linksink.sync.providers.SyncProviderRegistry(emptyList())
        )

        assertEquals(0, repository.getPendingSyncCount().first())
    }
}

private class PendingCountLinkDao(pendingCount: Int) : LinkDao {
    private val pending = MutableStateFlow(pendingCount)

    override fun getAllLinks(): Flow<List<LinkEntity>> = flowOf(emptyList())
    override suspend fun getPendingLinks(): List<LinkEntity> = emptyList()
    override suspend fun getLinkById(id: Long): LinkEntity? = null
    override suspend fun insert(link: LinkEntity): Long = 0
    override suspend fun update(link: LinkEntity) = Unit
    override suspend fun delete(link: LinkEntity) = Unit
    override suspend fun updateSyncStatus(id: Long, status: String, messageId: String?) = Unit
    override suspend fun incrementRetryCount(id: Long) = Unit
    override suspend fun markFailed(id: Long) = Unit
    override fun getPendingCount(): Flow<Int> = pending
    override fun getTotalCount(): Flow<Int> = flowOf(0)
    override fun getFiltered(topicId: Long?, startDate: Long?, endDate: Long?): Flow<List<LinkEntity>> = flowOf(emptyList())
    override suspend fun updateTopic(linkId: Long, topicId: Long?) = Unit
    override suspend fun updateMetadata(linkId: Long, title: String?, description: String?, thumbnailUrl: String?) = Unit
}

private class EmptyTopicDao : TopicDao {
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

private class PendingCountSettingsStore(
    syncSettings: SyncSettings
) : SyncSettingsStore {
    override val webhookUrl: Flow<String?> = flowOf(null)
    override val syncSettings: Flow<SyncSettings> = flowOf(syncSettings)
}

private class NoopMetadataFetcher : MetadataFetcherPort {
    override suspend fun fetch(url: String): Result<com.linksink.model.LinkMetadata> =
        Result.failure(IllegalStateException("disabled in unit tests"))
}

