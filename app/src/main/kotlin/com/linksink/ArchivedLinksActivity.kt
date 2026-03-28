package com.linksink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.linksink.ui.ArchivedLinksScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.viewmodel.LinkFilter
import com.linksink.viewmodel.LinkListViewModel

class ArchivedLinksActivity : ComponentActivity() {

    private lateinit var viewModel: LinkListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp

        viewModel = LinkListViewModel(
            repository = app.repository,
            topicRepository = app.topicRepository,
            settingsStore = app.settingsStore
        )
        viewModel.setLinkFilter(LinkFilter.ARCHIVED)

        setContent {
            LinkSinkTheme {
                ArchivedLinksScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
