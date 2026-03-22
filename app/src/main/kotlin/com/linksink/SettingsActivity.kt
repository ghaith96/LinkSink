package com.linksink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.linksink.ui.SettingsScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.viewmodel.SettingsViewModel

class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp
        viewModel = SettingsViewModel(
            settingsStore = app.settingsStore,
            discordClient = app.discordClient
        )

        setContent {
            LinkSinkTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onSettingsSaved = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
