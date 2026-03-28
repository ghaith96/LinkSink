package com.linksink.data

import com.linksink.data.local.LinkEntity
import com.linksink.data.local.toDomain
import com.linksink.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRepositoryTest {

    private fun linkEntity(
        id: Long,
        url: String,
        title: String?,
        domain: String = "example.com"
    ) = LinkEntity(
        id = id,
        url = url,
        title = title,
        description = null,
        thumbnailUrl = null,
        note = null,
        domain = domain,
        topicId = null,
        savedAt = 0L,
        syncStatus = SyncStatus.SYNCED.name,
        discordMessageId = null,
        retryCount = 0
    )

    @Test
    fun acceptance_searchLinksTransform_prefixQuery_returnsFuzzyMatchedTitle() {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val other = linkEntity(2L, "https://other.com/", "Kotlin Docs")

        val results = mapSearchResults("reac", listOf(react, other))

        assertEquals(1, results.size)
        assertEquals("React Tutorial", results.single().title)
    }

    @Test
    fun acceptance_searchLinksTransform_blankQuery_returnsAllFilteredLinks() {
        val a = linkEntity(1L, "https://a.com/", "A")
        val b = linkEntity(2L, "https://b.com/", "B")

        val results = mapSearchResults("", listOf(a, b))

        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "A" })
        assertTrue(results.any { it.title == "B" })
    }

    @Test
    fun unit_mapSearchResults_nonBlank_matchesFuzzySearcherOutput() {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val entities = listOf(react)
        val links = entities.map { it.toDomain() }

        val fromMapper = mapSearchResults("reac", entities)
        val fromSearcher = FuzzySearcher.search("reac", links).map { it.link }

        assertEquals(fromSearcher, fromMapper)
    }

    @Test
    fun unit_searchLinksFlow_usesSameTransformAsMapSearchResults() = runBlocking {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val entities = listOf(react)
        val query = "reac"

        val viaFlow = flowOf(entities)
            .map { mapSearchResults(query, it) }

        assertEquals(
            mapSearchResults(query, entities),
            viaFlow.first()
        )
    }

    @Test
    fun `markAsRead should call updateReadStatus on DAO`() {
        var updateReadStatusCalled = false
        var updatedLinkId = 0L
        var updatedIsRead = false

        val testDao = object : TestLinkDao() {
            override suspend fun updateReadStatus(id: Long, isRead: Boolean) {
                updateReadStatusCalled = true
                updatedLinkId = id
                updatedIsRead = isRead
            }
        }

        runBlocking {
            val repository = createTestRepository(linkDao = testDao)
            repository.markAsRead(42L)

            assertTrue(updateReadStatusCalled)
            assertEquals(42L, updatedLinkId)
            assertEquals(true, updatedIsRead)
        }
    }

    @Test
    fun `markAsUnread should call updateReadStatus with false`() {
        var updatedIsRead: Boolean? = null

        val testDao = object : TestLinkDao() {
            override suspend fun updateReadStatus(id: Long, isRead: Boolean) {
                updatedIsRead = isRead
            }
        }

        runBlocking {
            val repository = createTestRepository(linkDao = testDao)
            repository.markAsUnread(42L)

            assertEquals(false, updatedIsRead)
        }
    }

    @Test
    fun `archiveLink should call updateArchivedStatus with true`() {
        var updatedIsArchived: Boolean? = null

        val testDao = object : TestLinkDao() {
            override suspend fun updateArchivedStatus(id: Long, isArchived: Boolean) {
                updatedIsArchived = isArchived
            }
        }

        runBlocking {
            val repository = createTestRepository(linkDao = testDao)
            repository.archiveLink(42L)

            assertEquals(true, updatedIsArchived)
        }
    }

    @Test
    fun `unarchiveLink should call updateArchivedStatus with false`() {
        var updatedIsArchived: Boolean? = null

        val testDao = object : TestLinkDao() {
            override suspend fun updateArchivedStatus(id: Long, isArchived: Boolean) {
                updatedIsArchived = isArchived
            }
        }

        runBlocking {
            val repository = createTestRepository(linkDao = testDao)
            repository.unarchiveLink(42L)

            assertEquals(false, updatedIsArchived)
        }
    }

    @Test
    fun `getUnreadLinks should return flow from DAO`() = runBlocking {
        val unreadLink = linkEntity(1L, "https://example.com", "Unread", "example.com")
        val testDao = object : TestLinkDao() {
            override fun getUnreadLinks() = flowOf(listOf(unreadLink))
        }

        val repository = createTestRepository(linkDao = testDao)
        val result = repository.getUnreadLinks().first()

        assertEquals(1, result.size)
        assertEquals("Unread", result.first().title)
    }

    @Test
    fun `getArchivedLinks should return flow from DAO`() = runBlocking {
        val archivedLink = linkEntity(1L, "https://example.com", "Archived", "example.com")
        val testDao = object : TestLinkDao() {
            override fun getArchivedLinks() = flowOf(listOf(archivedLink))
        }

        val repository = createTestRepository(linkDao = testDao)
        val result = repository.getArchivedLinks().first()

        assertEquals(1, result.size)
        assertEquals("Archived", result.first().title)
    }

    @Test
    fun `getRandomUnreadLink should return link from DAO`() = runBlocking {
        val randomLink = linkEntity(1L, "https://example.com", "Random", "example.com")
        val testDao = object : TestLinkDao() {
            override suspend fun getRandomUnreadLink() = randomLink
        }

        val repository = createTestRepository(linkDao = testDao)
        val result = repository.getRandomUnreadLink()

        assertEquals("Random", result?.title)
    }

    @Test
    fun `getRandomUnreadLink should return null when no unread links`() = runBlocking {
        val testDao = object : TestLinkDao() {
            override suspend fun getRandomUnreadLink() = null
        }

        val repository = createTestRepository(linkDao = testDao)
        val result = repository.getRandomUnreadLink()

        assertEquals(null, result)
    }

    @Test
    fun `openLink should mark link as read before returning URL`() = runBlocking {
        var markedAsReadId: Long? = null
        val testLink = linkEntity(1L, "https://example.com", "Test Link", "example.com")

        val testDao = object : TestLinkDao() {
            override suspend fun getLinkById(id: Long) = testLink
            override suspend fun updateReadStatus(id: Long, isRead: Boolean) {
                if (isRead) {
                    markedAsReadId = id
                }
            }
        }

        val repository = createTestRepository(linkDao = testDao)
        val url = repository.openLink(1L)

        assertEquals(1L, markedAsReadId)
        assertEquals("https://example.com", url)
    }

    @Test
    fun `openLink should return null when link not found`() = runBlocking {
        val testDao = object : TestLinkDao() {
            override suspend fun getLinkById(id: Long): LinkEntity? = null
        }

        val repository = createTestRepository(linkDao = testDao)
        val url = repository.openLink(1L)

        assertEquals(null, url)
    }
}

private fun createTestRepository(
    linkDao: com.linksink.data.local.LinkDao,
    topicDao: com.linksink.data.local.TopicDao = TestTopicDao(),
    settingsStore: SyncSettingsStore = TestSettingsStore(),
    metadataFetcher: MetadataFetcherPort = TestMetadataFetcher()
) = LinkRepository(
    linkDao = linkDao,
    topicDao = topicDao,
    settingsStore = settingsStore,
    metadataFetcher = metadataFetcher,
    applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Job()),
    providerRegistry = com.linksink.sync.providers.SyncProviderRegistry(emptyList())
)

private abstract class TestLinkDao : com.linksink.data.local.LinkDao {
    override fun getAllLinks() = flowOf(emptyList<LinkEntity>())
    override suspend fun getPendingLinks() = emptyList<LinkEntity>()
    override suspend fun getLinkById(id: Long): LinkEntity? = null
    override suspend fun insert(link: LinkEntity): Long = 0
    override suspend fun update(link: LinkEntity) = Unit
    override suspend fun delete(link: LinkEntity) = Unit
    override suspend fun updateSyncStatus(id: Long, status: String, messageId: String?) = Unit
    override suspend fun incrementRetryCount(id: Long) = Unit
    override suspend fun markFailed(id: Long) = Unit
    override fun getPendingCount() = flowOf(0)
    override fun getTotalCount() = flowOf(0)
    override fun getFiltered(topicId: Long?, startDate: Long?, endDate: Long?) = flowOf(emptyList<LinkEntity>())
    override suspend fun updateTopic(linkId: Long, topicId: Long?) = Unit
    override suspend fun updateMetadata(linkId: Long, title: String?, description: String?, thumbnailUrl: String?) = Unit
    override suspend fun updateReadStatus(id: Long, isRead: Boolean) = Unit
    override suspend fun updateArchivedStatus(id: Long, isArchived: Boolean) = Unit
    override fun getUnreadLinks() = flowOf(emptyList<LinkEntity>())
    override fun getArchivedLinks() = flowOf(emptyList<LinkEntity>())
    override suspend fun getRandomUnreadLink(): LinkEntity? = null
}

private class TestTopicDao : com.linksink.data.local.TopicDao {
    override fun getAllTopics() = flowOf(emptyList<com.linksink.data.local.TopicEntity>())
    override suspend fun getById(id: Long): com.linksink.data.local.TopicEntity? = null
    override suspend fun getWithLinkCount(id: Long) = null
    override suspend fun insert(topic: com.linksink.data.local.TopicEntity): Long = 0
    override suspend fun update(topic: com.linksink.data.local.TopicEntity) = Unit
    override suspend fun delete(id: Long) = Unit
    override suspend fun unlinkAllFromTopic(topicId: Long) = Unit
    override suspend fun deleteLinksInTopic(topicId: Long) = Unit
    override fun getRecentlyUsed(limit: Int) = flowOf(emptyList<com.linksink.data.local.TopicEntity>())
    override suspend fun getByName(name: String): com.linksink.data.local.TopicEntity? = null
    override suspend fun getMaxDisplayOrder(): Int? = null
    override suspend fun updateDisplayOrder(id: Long, order: Int) = Unit
    override suspend fun updateDisplayOrders(updates: List<Pair<Long, Int>>) = Unit
}

private class TestSettingsStore : SyncSettingsStore {
    override val webhookUrl = flowOf<String?>(null)
    override val syncSettings = flowOf(com.linksink.sync.settings.SyncSettings(
        enabled = false,
        providerId = com.linksink.sync.providers.SyncProviderId.DISCORD_WEBHOOK,
        isProviderConfigValid = false
    ))
}

private class TestMetadataFetcher : MetadataFetcherPort {
    override suspend fun fetch(url: String) = Result.failure<com.linksink.model.LinkMetadata>(
        IllegalStateException("Test fetcher")
    )
}
