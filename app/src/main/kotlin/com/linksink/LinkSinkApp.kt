package com.linksink

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.linksink.data.LinkRepository
import com.linksink.data.MetadataFetcher
import com.linksink.data.SettingsStore
import com.linksink.data.TopicRepository
import com.linksink.data.local.LinkDatabase
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.sync.SyncManager
import com.linksink.sync.SyncWorker
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

class LinkSinkApp : Application(), DefaultLifecycleObserver {

    lateinit var settingsStore: SettingsStore
        private set

    lateinit var discordClient: DiscordWebhookClient
        private set

    lateinit var repository: LinkRepository
        private set

    lateinit var topicRepository: TopicRepository
        private set

    lateinit var syncManager: SyncManager
        private set

    private lateinit var database: LinkDatabase
    private lateinit var metadataFetcher: MetadataFetcher
    private lateinit var httpClient: HttpClient
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super<Application>.onCreate()

        database = LinkDatabase.getInstance(this)
        settingsStore = SettingsStore(this)
        discordClient = DiscordWebhookClient()

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        metadataFetcher = MetadataFetcher(httpClient)

        topicRepository = TopicRepository(
            topicDao = database.topicDao()
        )

        repository = LinkRepository(
            linkDao = database.linkDao(),
            topicDao = database.topicDao(),
            discordClient = discordClient,
            settingsStore = settingsStore,
            metadataFetcher = metadataFetcher,
            applicationScope = applicationScope
        )

        syncManager = SyncManager(
            context = this,
            repository = repository
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        SyncWorker.enqueuePeriodicSync(this)

        Log.d(TAG, "LinkSinkApp initialized")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "App foregrounded, starting sync")
        syncManager.startNetworkObserver()
        syncManager.triggerSync()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "App backgrounded")
        syncManager.stopNetworkObserver()
    }

    override fun onTerminate() {
        super.onTerminate()
        discordClient.close()
        httpClient.close()
        syncManager.stopNetworkObserver()
    }

    companion object {
        private const val TAG = "LinkSinkApp"
    }
}
