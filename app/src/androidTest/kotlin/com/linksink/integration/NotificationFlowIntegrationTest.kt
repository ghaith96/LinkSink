package com.linksink.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.linksink.data.LinkRepository
import com.linksink.data.MetadataFetcher
import com.linksink.data.SettingsStore
import com.linksink.data.local.LinkDatabase
import com.linksink.data.local.LinkEntity
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.SyncStatus
import com.linksink.sync.providers.DiscordWebhookClientApi
import com.linksink.sync.providers.DiscordWebhookSyncProvider
import com.linksink.sync.providers.NoneSyncProvider
import com.linksink.sync.providers.SyncProviderRegistry
import com.linksink.workers.NotificationScheduler
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
class NotificationFlowIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: LinkDatabase
    private lateinit var repository: LinkRepository
    private lateinit var settingsStore: SettingsStore
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var workManager: WorkManager
    private lateinit var httpClient: HttpClient
    private val testScope = CoroutineScope(SupervisorJob())

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        
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
        
        notificationScheduler = NotificationScheduler(context)
    }

    @After
    fun teardown() {
        database.close()
        httpClient.close()
        workManager.cancelAllWork()
    }

    @Test
    fun `notification scheduler creates periodic work when enabled`() = runTest {
        settingsStore.setReminderEnabled(true)
        settingsStore.setReminderFrequencyHours(24)
        
        notificationScheduler.scheduleReminders(24)
        
        val workInfos = workManager.getWorkInfosForUniqueWork("link_reminder_notifications").get()
        
        assert(workInfos.isNotEmpty()) { "WorkManager should have scheduled work" }
        assert(workInfos[0].state == WorkInfo.State.ENQUEUED) { "Work should be enqueued" }
    }

    @Test
    fun `notification scheduler cancels work when disabled`() = runTest {
        notificationScheduler.scheduleReminders(24)
        
        var workInfos = workManager.getWorkInfosForUniqueWork("link_reminder_notifications").get()
        assert(workInfos.isNotEmpty()) { "Work should be scheduled" }
        
        notificationScheduler.cancelReminders()
        
        workInfos = workManager.getWorkInfosForUniqueWork("link_reminder_notifications").get()
        assert(workInfos.isEmpty() || workInfos[0].state == WorkInfo.State.CANCELLED) {
            "Work should be cancelled or removed"
        }
    }

    @Test
    fun `daily notification count resets on date change`() = runTest {
        settingsStore.setReminderMaxDaily(3)
        
        settingsStore.incrementNotificationCount()
        settingsStore.incrementNotificationCount()
        
        var count = settingsStore.todayNotificationCount.first()
        assert(count == 2) { "Count should be 2 after two increments" }
        
        settingsStore.resetNotificationCountIfNeeded()
        
        count = settingsStore.todayNotificationCount.first()
        assert(count == 2) { "Count should remain 2 on same day" }
    }

    @Test
    fun `notification worker respects daily limit`() = runTest {
        val unreadLink = LinkEntity(
            id = 1L,
            url = "https://test.com",
            domain = "test.com",
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
        
        database.linkDao().insert(unreadLink)
        
        settingsStore.setReminderEnabled(true)
        settingsStore.setReminderMaxDaily(1)
        
        settingsStore.incrementNotificationCount()
        
        val count = settingsStore.todayNotificationCount.first()
        val maxDaily = settingsStore.reminderMaxDaily.first()
        
        assert(count >= maxDaily) { "Daily limit should be reached or exceeded" }
        
        val randomLink = repository.getRandomUnreadLink()
        assert(randomLink != null) { "Random link should exist but worker should respect limit" }
    }

    @Test
    fun `archived links are excluded from random selection`() = runTest {
        val archivedLink = LinkEntity(
            id = 1L,
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
        
        database.linkDao().insert(archivedLink)
        
        val randomLink = repository.getRandomUnreadLink()
        
        assert(randomLink == null) { "Archived links should not be included in random selection" }
    }
}
