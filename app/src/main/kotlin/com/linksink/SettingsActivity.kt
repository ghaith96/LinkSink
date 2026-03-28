package com.linksink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.linksink.ui.SettingsScreen
import com.linksink.ui.TopicManagementScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.viewmodel.SettingsViewModel
import com.linksink.viewmodel.TopicViewModel

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var topicViewModel: TopicViewModel
    private var showTopicManagement by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp
        settingsViewModel = SettingsViewModel(
            settingsStore = app.settingsStore,
            discordClient = app.discordClient
        )
        topicViewModel = TopicViewModel(
            topicRepository = app.topicRepository,
            discordClient = app.discordClient,
            settingsStore = app.settingsStore
        )

        setContent {
            LinkSinkTheme {
                if (showTopicManagement) {
                    TopicManagementScreen(
                        viewModel = topicViewModel,
                        onBack = { showTopicManagement = false }
                    )
                } else {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onSettingsSaved = {
                            finish()
                        },
                        onManageTopicsClick = { showTopicManagement = true }
                    )
                }
            }
        }
    }
}
