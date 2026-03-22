package com.linksink

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.linksink.data.LinkRepository
import com.linksink.data.SettingsStore
import com.linksink.data.local.LinkDatabase
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.sync.SyncManager
import com.linksink.sync.SyncWorker

class LinkSinkApp : Application(), DefaultLifecycleObserver {

    lateinit var settingsStore: SettingsStore
        private set

    lateinit var discordClient: DiscordWebhookClient
        private set

    lateinit var repository: LinkRepository
        private set

    lateinit var syncManager: SyncManager
        private set

    private lateinit var database: LinkDatabase

    override fun onCreate() {
        super<Application>.onCreate()

        database = LinkDatabase.getInstance(this)
        settingsStore = SettingsStore(this)
        discordClient = DiscordWebhookClient()

        repository = LinkRepository(
            linkDao = database.linkDao(),
            discordClient = discordClient,
            settingsStore = settingsStore
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
        syncManager.stopNetworkObserver()
    }

    companion object {
        private const val TAG = "LinkSinkApp"
    }
}
