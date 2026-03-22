package com.linksink

import android.app.Application
import com.linksink.data.LinkRepository
import com.linksink.data.SettingsStore
import com.linksink.data.local.LinkDatabase
import com.linksink.data.remote.DiscordWebhookClient

class LinkSinkApp : Application() {

    lateinit var settingsStore: SettingsStore
        private set

    lateinit var discordClient: DiscordWebhookClient
        private set

    lateinit var repository: LinkRepository
        private set

    private lateinit var database: LinkDatabase

    override fun onCreate() {
        super.onCreate()

        database = LinkDatabase.getInstance(this)
        settingsStore = SettingsStore(this)
        discordClient = DiscordWebhookClient()

        repository = LinkRepository(
            linkDao = database.linkDao(),
            discordClient = discordClient,
            settingsStore = settingsStore
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        discordClient.close()
    }
}
