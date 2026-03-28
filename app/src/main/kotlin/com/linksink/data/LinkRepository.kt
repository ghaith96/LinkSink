package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.LinkEntity
import com.linksink.data.local.TopicDao
import com.linksink.data.local.toDomain
import com.linksink.data.local.toEntity
import com.linksink.model.DateRange
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.WebhookResolution
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.providers.SyncProviderConfig
import com.linksink.sync.providers.SyncProviderRegistry
import com.linksink.sync.settings.SyncSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.Instant

class LinkRepository(
    private val linkDao: LinkDao,
    private val topicDao: TopicDao,
    private val settingsStore: SyncSettingsStore,
    private val metadataFetcher: MetadataFetcherPort,
    private val applicationScope: CoroutineScope,
    private val providerRegistry: SyncProviderRegistry
) {

    fun getLinks(): Flow<List<Link>> {
        return linkDao.getAllLinks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getLinkById(id: Long): Link? {
        return linkDao.getLinkById(id)?.toDomain()
    }

    fun getPendingSyncCount(): Flow<Int> {
        return combine(
            settingsStore.syncSettings,
            linkDao.getPendingCount()
        ) { syncSettings, pendingCount ->
            if (syncSettings.enabled) pendingCount else 0
        }
    }

    suspend fun saveLink(
        url: String,
        topicId: Long? = null,
        note: String? = null
    ): Result<Link> = withContext(Dispatchers.IO) {
        try {
            val trimmedUrl = url.trim()
            if (!isValidUrl(trimmedUrl)) {
                return@withContext Result.failure(InvalidUrlException(trimmedUrl))
            }

            val domain = extractDomain(trimmedUrl)
            val syncSettings = settingsStore.syncSettings.first()
            val topic = topicId?.let { topicDao.getById(it)?.toDomain() }
            val globalWebhookUrl = settingsStore.webhookUrl.first()
            val resolution = WebhookRouter.resolve(topic, globalWebhookUrl)
            val initialSyncStatus = initialSyncStatusForSave(syncSettings, resolution)

            val link = Link(
                url = trimmedUrl,
                title = null,
                description = null,
                thumbnailUrl = null,
                note = note,
                domain = domain,
                topicId = topicId,
                savedAt = Instant.now(),
                syncStatus = initialSyncStatus
            )

            val id = linkDao.insert(link.toEntity())
            val savedLink = link.copy(id = id)

            applicationScope.launch(Dispatchers.IO) {
                fetchAndUpdateMetadata(id, trimmedUrl)
            }

            if (initialSyncStatus == SyncStatus.PENDING) syncLink(savedLink, syncSettings)

            val updatedLink = linkDao.getLinkById(id)?.toDomain() ?: savedLink
            Result.success(updatedLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchLinks(
        query: String,
        topicId: Long?,
        dateRange: DateRange?
    ): Flow<List<Link>> {
        val startDate = dateRange?.start?.toEpochMilli()
        val endDate = dateRange?.end?.toEpochMilli()

        return linkDao.getFiltered(topicId, startDate, endDate)
            .map { entities -> mapSearchResults(query, entities) }
    }

    suspend fun updateLinkTopic(linkId: Long, topicId: Long?) {
        linkDao.updateTopic(linkId, topicId)
    }

    private suspend fun fetchAndUpdateMetadata(linkId: Long, url: String) {
        val result = metadataFetcher.fetch(url)
        if (result.isSuccess) {
            val metadata = result.getOrNull()
            if (metadata != null && !metadata.isEmpty) {
                linkDao.updateMetadata(
                    linkId = linkId,
                    title = metadata.title,
                    description = metadata.description,
                    thumbnailUrl = metadata.imageUrl
                )
            }
        } else {
            val domain = extractDomain(url)
            linkDao.updateMetadata(
                linkId = linkId,
                title = domain,
                description = null,
                thumbnailUrl = null
            )
        }
    }

    suspend fun deleteLink(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = linkDao.getLinkById(id)
            if (entity != null) {
                linkDao.delete(entity)
                Result.success(Unit)
            } else {
                Result.failure(LinkNotFoundException(id))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(linkId: Long) {
        linkDao.updateReadStatus(linkId, isRead = true)
    }

    suspend fun markAsUnread(linkId: Long) {
        linkDao.updateReadStatus(linkId, isRead = false)
    }

    suspend fun toggleReadStatus(linkId: Long) {
        val link = linkDao.getLinkById(linkId)
        if (link != null) {
            linkDao.updateReadStatus(linkId, !link.isRead)
        }
    }

    suspend fun archiveLink(linkId: Long) {
        linkDao.updateArchivedStatus(linkId, isArchived = true)
    }

    suspend fun unarchiveLink(linkId: Long) {
        linkDao.updateArchivedStatus(linkId, isArchived = false)
    }

    fun getUnreadLinks(): Flow<List<Link>> {
        return linkDao.getUnreadLinks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getArchivedLinks(): Flow<List<Link>> {
        return linkDao.getArchivedLinks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getRandomUnreadLink(): Link? {
        return linkDao.getRandomUnreadLink()?.toDomain()
    }

    suspend fun openLink(linkId: Long): String? {
        val entity = linkDao.getLinkById(linkId)
        if (entity != null) {
            markAsRead(linkId)
            return entity.url
        }
        return null
    }

    suspend fun syncPendingLinks(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val syncSettings = settingsStore.syncSettings.first()
            if (!syncSettings.enabled || !syncSettings.isProviderConfigValid) {
                return@withContext Result.success(0)
            }

            val pendingLinks = linkDao.getPendingLinks()
            var syncedCount = 0

            for (entity in pendingLinks) {
                val link = entity.toDomain()
                if (syncLink(link, syncSettings)) {
                    syncedCount++
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncLink(link: Link): Boolean {
        val syncSettings = settingsStore.syncSettings.first()
        return syncLink(link, syncSettings)
    }

    private suspend fun syncLink(link: Link, syncSettings: SyncSettings): Boolean {
        if (!syncSettings.enabled || !syncSettings.isProviderConfigValid) return false
        val provider = providerRegistry.resolve(syncSettings.providerId) ?: return false

        val topic = link.topicId?.let { topicDao.getById(it)?.toDomain() }
        val globalWebhookUrl = settingsStore.webhookUrl.first()

        return when (val resolution = WebhookRouter.resolve(topic, globalWebhookUrl)) {
            is WebhookResolution.Send -> {
                val config = SyncProviderConfig(
                    providerId = syncSettings.providerId,
                    webhookUrl = resolution.webhookUrl
                )
                val result = provider.sendLink(config, link)
                if (result.isSuccess) {
                    linkDao.updateSyncStatus(
                        id = link.id,
                        status = SyncStatus.SYNCED.name,
                        messageId = result.getOrNull()
                    )
                    true
                } else {
                    linkDao.incrementRetryCount(link.id)
                    val updatedEntity = linkDao.getLinkById(link.id)
                    if (updatedEntity != null && updatedEntity.retryCount >= MAX_RETRY_COUNT) {
                        linkDao.markFailed(link.id)
                    }
                    false
                }
            }
            is WebhookResolution.LocalOnly -> {
                linkDao.updateSyncStatus(
                    id = link.id,
                    status = SyncStatus.LOCAL_ONLY.name,
                    messageId = null
                )
                true
            }
            is WebhookResolution.NoWebhookConfigured -> {
                false
            }
        }
    }

    companion object {
        private const val MAX_RETRY_COUNT = 5
        private val URL_REGEX = Regex(
            """https?://[^\s<>"{}|\\^`\[\]]+""",
            RegexOption.IGNORE_CASE
        )

        fun extractUrlFromText(text: String): String? {
            return URL_REGEX.find(text)?.value
        }

        fun isValidUrl(url: String): Boolean {
            return try {
                val parsed = URL(url)
                parsed.protocol in listOf("http", "https")
            } catch (e: Exception) {
                false
            }
        }

        fun extractDomain(url: String): String {
            return try {
                URL(url).host.removePrefix("www.")
            } catch (e: Exception) {
                url
            }
        }
    }
}

internal fun initialSyncStatusForSave(
    syncSettings: SyncSettings,
    resolution: WebhookResolution
): SyncStatus =
    when (resolution) {
        is WebhookResolution.LocalOnly -> SyncStatus.LOCAL_ONLY
        else ->
            if (
                syncSettings.enabled &&
                syncSettings.providerId == SyncProviderId.DISCORD_WEBHOOK &&
                syncSettings.isProviderConfigValid
            ) {
                SyncStatus.PENDING
            } else {
                SyncStatus.LOCAL_ONLY
            }
    }

internal fun mapSearchResults(query: String, entities: List<LinkEntity>): List<Link> {
    val links = entities.map { it.toDomain() }
    return if (query.isBlank()) {
        links
    } else {
        FuzzySearcher.search(query, links).map { it.link }
    }
}

class InvalidUrlException(val url: String) : Exception("Invalid URL: $url")
class LinkNotFoundException(val id: Long) : Exception("Link not found: $id")
