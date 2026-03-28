package com.linksink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.linksink.ui.settings.NotificationSettingsScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.viewmodel.NotificationSettingsViewModel
import com.linksink.viewmodel.NotificationSettingsViewModelFactory

class NotificationSettingsActivity : ComponentActivity() {

    private lateinit var viewModel: NotificationSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp

        val factory = NotificationSettingsViewModelFactory(app.settingsStore)
        viewModel = ViewModelProvider(this, factory)[NotificationSettingsViewModel::class.java]

        setContent {
            LinkSinkTheme {
                NotificationSettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
