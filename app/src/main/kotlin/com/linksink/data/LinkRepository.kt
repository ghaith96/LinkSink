package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.LinkEntity
import com.linksink.data.local.TopicDao
import com.linksink.data.local.toDomain
import com.linksink.data.local.toEntity
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.DateRange
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.WebhookResolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.Instant

class LinkRepository(
    private val linkDao: LinkDao,
    private val topicDao: TopicDao,
    private val discordClient: DiscordWebhookClient,
    private val settingsStore: SettingsStore,
    private val metadataFetcher: MetadataFetcher,
    private val applicationScope: CoroutineScope
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
        return linkDao.getPendingCount()
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
            val link = Link(
                url = trimmedUrl,
                title = null,
                description = null,
                thumbnailUrl = null,
                note = note,
                domain = domain,
                topicId = topicId,
                savedAt = Instant.now(),
                syncStatus = SyncStatus.PENDING
            )

            val id = linkDao.insert(link.toEntity())
            val savedLink = link.copy(id = id)

            applicationScope.launch(Dispatchers.IO) {
                fetchAndUpdateMetadata(id, trimmedUrl)
            }

            syncToDiscord(savedLink)

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

    suspend fun syncPendingLinks(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pendingLinks = linkDao.getPendingLinks()
            var syncedCount = 0

            for (entity in pendingLinks) {
                val link = entity.toDomain()
                if (syncToDiscord(link)) {
                    syncedCount++
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncToDiscord(link: Link): Boolean {
        val topic = link.topicId?.let { topicDao.getById(it)?.toDomain() }
        val globalWebhookUrl = settingsStore.webhookUrl.first()

        return when (val resolution = WebhookRouter.resolve(topic, globalWebhookUrl)) {
            is WebhookResolution.Send -> {
                val result = discordClient.sendLink(resolution.webhookUrl, link)
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
                    status = SyncStatus.SYNCED.name,
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
