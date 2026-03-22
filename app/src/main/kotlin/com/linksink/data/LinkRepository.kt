package com.linksink.data

import com.linksink.data.local.LinkDao
import com.linksink.data.local.toDomain
import com.linksink.data.local.toEntity
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.Instant

class LinkRepository(
    private val linkDao: LinkDao,
    private val discordClient: DiscordWebhookClient,
    private val settingsStore: SettingsStore
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

    suspend fun saveLink(url: String, note: String? = null): Result<Link> = withContext(Dispatchers.IO) {
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
                savedAt = Instant.now(),
                syncStatus = SyncStatus.PENDING
            )

            val id = linkDao.insert(link.toEntity())
            val savedLink = link.copy(id = id)

            syncToDiscord(savedLink)

            val updatedLink = linkDao.getLinkById(id)?.toDomain() ?: savedLink
            Result.success(updatedLink)
        } catch (e: Exception) {
            Result.failure(e)
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
        val webhookUrl = settingsStore.webhookUrl.first()
        if (webhookUrl.isNullOrBlank()) {
            return false
        }

        val result = discordClient.sendLink(webhookUrl, link)
        return if (result.isSuccess) {
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

class InvalidUrlException(val url: String) : Exception("Invalid URL: $url")
class LinkNotFoundException(val id: Long) : Exception("Link not found: $id")
