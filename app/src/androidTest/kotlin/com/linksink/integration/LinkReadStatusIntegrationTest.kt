package com.linksink.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.linksink.data.LinkRepository
import com.linksink.data.MetadataFetcher
import com.linksink.data.SettingsStore
import com.linksink.data.TopicRepository
import com.linksink.data.local.LinkDatabase
import com.linksink.data.local.LinkEntity
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.SyncStatus
import com.linksink.sync.providers.DiscordWebhookClientApi
import com.linksink.sync.providers.DiscordWebhookSyncProvider
import com.linksink.sync.providers.NoneSyncProvider
import com.linksink.sync.providers.SyncProviderRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LinkReadStatusIntegrationTest {

    private lateinit var database: LinkDatabase
    private lateinit var repository: LinkRepository
    private lateinit var settingsStore: SettingsStore
    private lateinit var httpClient: HttpClient
    private val testScope = CoroutineScope(SupervisorJob())

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        database = Room.inMemoryDatabaseBuilder(
            context,
            LinkDatabase::class.java
        ).allowMainThreadQueries().build()

        settingsStore = SettingsStore(context)

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        val metadataFetcher = MetadataFetcher(httpClient)
        
        val providerRegistry = SyncProviderRegistry(
            providers = listOf(
                NoneSyncProvider(),
                DiscordWebhookSyncProvider(
                    DiscordWebhookClientApi(DiscordWebhookClient())
                )
            )
        )

        repository = LinkRepository(
            linkDao = database.linkDao(),
            topicDao = database.topicDao(),
            settingsStore = settingsStore,
            metadataFetcher = metadataFetcher,
            applicationScope = testScope,
            providerRegistry = providerRegistry
        )
    }

    @After
    fun teardown() {
        database.close()
        httpClient.close()
    }

    @Test
    fun `openLink marks link as read automatically`() = runTest {
        val linkEntity = LinkEntity(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            title = "Test Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = false
        )
        
        database.linkDao().insert(linkEntity)
        
        val linkBefore = database.linkDao().getLinkById(1L)
        assert(linkBefore != null && !linkBefore.isRead) { "Link should initially be unread" }
        
        val url = repository.openLink(1L)
        
        assert(url == "https://example.com") { "openLink should return the URL" }
        
        val linkAfter = database.linkDao().getLinkById(1L)
        assert(linkAfter != null && linkAfter.isRead) { "Link should be marked as read after opening" }
    }

    @Test
    fun `marking link as read updates database`() = runTest {
        val linkEntity = LinkEntity(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            title = "Test Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = false
        )
        
        database.linkDao().insert(linkEntity)
        
        repository.markAsRead(1L)
        
        val link = database.linkDao().getLinkById(1L)
        assert(link != null && link.isRead) { "Link should be marked as read" }
    }

    @Test
    fun `archiving link updates database`() = runTest {
        val linkEntity = LinkEntity(
            id = 1L,
            url = "https://example.com",
            domain = "example.com",
            title = "Test Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = false
        )
        
        database.linkDao().insert(linkEntity)
        
        repository.archiveLink(1L)
        
        val link = database.linkDao().getLinkById(1L)
        assert(link != null && link.isArchived) { "Link should be archived" }
    }

    @Test
    fun `getRandomUnreadLink excludes read and archived links`() = runTest {
        val readLink = LinkEntity(
            id = 1L,
            url = "https://read.com",
            domain = "read.com",
            title = "Read Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = true,
            isArchived = false
        )
        
        val archivedLink = LinkEntity(
            id = 2L,
            url = "https://archived.com",
            domain = "archived.com",
            title = "Archived Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = true
        )
        
        val unreadLink = LinkEntity(
            id = 3L,
            url = "https://unread.com",
            domain = "unread.com",
            title = "Unread Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = false
        )
        
        database.linkDao().insert(readLink)
        database.linkDao().insert(archivedLink)
        database.linkDao().insert(unreadLink)
        
        val randomLink = repository.getRandomUnreadLink()
        
        assert(randomLink != null) { "Should find an unread link" }
        assert(randomLink?.id == 3L) { "Should return the unread link" }
        assert(!randomLink!!.isRead && !randomLink.isArchived) { "Returned link should be unread and not archived" }
    }

    @Test
    fun `getUnreadLinks returns only unread non-archived links`() = runTest {
        val readLink = LinkEntity(
            id = 1L,
            url = "https://read.com",
            domain = "read.com",
            title = "Read Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = true,
            isArchived = false
        )
        
        val unreadArchivedLink = LinkEntity(
            id = 2L,
            url = "https://archived.com",
            domain = "archived.com",
            title = "Unread Archived Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = true
        )
        
        val unreadLink = LinkEntity(
            id = 3L,
            url = "https://unread.com",
            domain = "unread.com",
            title = "Unread Link",
            description = null,
            thumbnailUrl = null,
            note = null,
            savedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            discordMessageId = null,
            retryCount = 0,
            isRead = false,
            isArchived = false
        )
        
        database.linkDao().insert(readLink)
        database.linkDao().insert(unreadArchivedLink)
        database.linkDao().insert(unreadLink)
        
        val unreadLinks = repository.getUnreadLinks().first()
        
        assert(unreadLinks.size == 1) { "Should have exactly 1 unread non-archived link" }
        assert(unreadLinks[0].id == 3L) { "Should return only the unread non-archived link" }
    }
}
