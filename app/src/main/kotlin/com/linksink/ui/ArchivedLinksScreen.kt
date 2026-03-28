package com.linksink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.LinkListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedLinksScreen(
    viewModel: LinkListViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Links") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is LinkListUiState.Success -> {
                val archivedLinks = state.links.filter { it.isArchived }
                if (archivedLinks.isEmpty()) {
                    EmptyContent(
                        message = "No archived links",
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(
                            items = archivedLinks,
                            key = { it.id }
                        ) { link ->
                            val linkTopic = topics.find { it.id == link.topicId }
                            SwipeableLinkCard(
                                link = link,
                                topicName = linkTopic?.name,
                                topicColor = linkTopic?.color,
                                topicEmoji = linkTopic?.emoji,
                                onDelete = { viewModel.deleteLink(link) },
                                onToggleRead = null,
                                startToEnd = SwipeStartToEnd(
                                    action = getSwipeAction(isArchived = true),
                                    onComplete = { viewModel.unarchiveLink(link) }
                                ),
                                onClick = {
                                    viewModel.openLink(link)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
            else -> EmptyContent(
                message = "No archived links",
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
