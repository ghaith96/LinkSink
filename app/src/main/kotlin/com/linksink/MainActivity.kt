package com.linksink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.linksink.ui.LinkListScreen
import com.linksink.ui.theme.LinkSinkTheme
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

        lifecycleScope.launch {
            val isOnboardingComplete = app.settingsStore.isOnboardingComplete.first()
            if (!isOnboardingComplete) {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                finish()
                return@launch
            }

            viewModel = LinkListViewModel(
                repository = app.repository,
                topicRepository = app.topicRepository,
                settingsStore = app.settingsStore
            )

            topicViewModel = TopicViewModel(
                topicRepository = app.topicRepository,
                discordClient = app.discordClient
            )

            setContent {
                LinkSinkTheme {
                    LinkListScreen(
                        viewModel = viewModel,
                        topicViewModel = topicViewModel,
                        onSettingsClick = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
