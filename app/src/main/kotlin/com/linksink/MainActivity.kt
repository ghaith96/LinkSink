package com.linksink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.linksink.notifications.NotificationHelper
import com.linksink.ui.LinkListScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.launch.LaunchDestination
import com.linksink.launch.LaunchDestinationDecider
import com.linksink.viewmodel.LinkListViewModel
import com.linksink.viewmodel.TopicViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LinkListViewModel
    private lateinit var topicViewModel: TopicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp

        viewModel = LinkListViewModel(
            repository = app.repository,
            topicRepository = app.topicRepository,
            settingsStore = app.settingsStore
        )

        topicViewModel = TopicViewModel(
            topicRepository = app.topicRepository,
            discordClient = app.discordClient,
            settingsStore = app.settingsStore
        )

        setContent {
            LinkSinkTheme {
                LinkListScreen(
                    viewModel = viewModel,
                    topicViewModel = topicViewModel,
                    onSettingsClick = {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    },
                    onArchivedClick = {
                        startActivity(Intent(this@MainActivity, ArchivedLinksActivity::class.java))
                    },
                    onNotificationsClick = {
                        startActivity(Intent(this@MainActivity, NotificationSettingsActivity::class.java))
                    }
                )
            }
        }

        // Read persisted settings, but never hard-block app usage.
        lifecycleScope.launch {
            val isOnboardingComplete = app.settingsStore.isOnboardingComplete.first()
            val hasWebhookUrl = !app.settingsStore.webhookUrl.first().isNullOrBlank()

            when (LaunchDestinationDecider().decide(isOnboardingComplete, hasWebhookUrl)) {
                LaunchDestination.Main -> Unit
                LaunchDestination.Settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            NotificationHelper.ACTION_OPEN_LINK -> {
                val linkId = intent.getLongExtra(NotificationHelper.EXTRA_LINK_ID, -1L)
                if (linkId != -1L) {
                    lifecycleScope.launch {
                        val url = (application as LinkSinkApp).repository.openLink(linkId)
                        if (url != null) {
                            val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(openIntent)
                        }
                    }
                }
            }
        }
    }
}
