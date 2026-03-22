package com.linksink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import com.linksink.ui.components.DateRangePickerSheet
import com.linksink.ui.components.FilterChips
import com.linksink.ui.components.SearchBar
import com.linksink.ui.components.TopicChipSmall
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.LinkListViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkListScreen(
    viewModel: LinkListViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val topicFilter by viewModel.topicFilter.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val context = LocalContext.current

    var showTopicDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedTopicName = topics.find { it.id == topicFilter }?.name

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinkSink") },
                actions = {
                    val pendingCount = (uiState as? LinkListUiState.Success)?.pendingCount ?: 0
                    if (pendingCount > 0) {
                        IconButton(onClick = { viewModel.syncPendingLinks() }) {
                            BadgedBox(
                                badge = {
                                    Badge { Text(pendingCount.toString()) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync pending"
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showTopicDropdown = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter by topic"
                            )
                        }
                        TopicFilterDropdown(
                            expanded = showTopicDropdown,
                            topics = topics,
                            selectedTopicId = topicFilter,
                            onTopicSelected = { 
                                viewModel.setTopicFilter(it)
                                showTopicDropdown = false
                            },
                            onDismiss = { showTopicDropdown = false }
                        )
                    }

                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Filter by date"
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )

            if (topicFilter != null || dateRange != null) {
                FilterChips(
                    topicName = selectedTopicName,
                    dateRange = dateRange,
                    onClearTopic = { viewModel.setTopicFilter(null) },
                    onClearDateRange = { viewModel.setDateRange(null) },
                    onClearAll = { viewModel.clearFilters() }
                )
            }

            when (val state = uiState) {
                is LinkListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LinkListUiState.Empty -> {
                    EmptyContent(message = state.message)
                }

                is LinkListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.links,
                            key = { it.id }
                        ) { link ->
                            val linkTopicName = topics.find { it.id == link.topicId }?.name
                            SwipeableLinkCard(
                                link = link,
                                topicName = linkTopicName,
                                onDelete = { viewModel.deleteLink(link) },
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                is LinkListUiState.Error -> {
                    ErrorContent(message = state.message)
                }
            }
        }

        if (showDatePicker) {
            DateRangePickerSheet(
                currentRange = dateRange,
                onRangeSelected = { viewModel.setDateRange(it) },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun TopicFilterDropdown(
    expanded: Boolean,
    topics: List<Topic>,
    selectedTopicId: Long?,
    onTopicSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("All Topics") },
            onClick = { onTopicSelected(null) }
        )
        DropdownMenuItem(
            text = { Text("Uncategorized") },
            onClick = { onTopicSelected(-1L) }
        )
        topics.forEach { topic ->
            DropdownMenuItem(
                text = { Text(topic.name) },
                onClick = { onTopicSelected(topic.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableLinkCard(
    link: Link,
    topicName: String?,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        LinkCard(link = link, topicName = topicName, onClick = onClick)
    }
}

@Composable
private fun LinkCard(
    link: Link,
    topicName: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title ?: link.domain,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTimestamp(link),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (topicName != null) {
                        TopicChipSmall(topicName = topicName)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            SyncStatusIcon(link.syncStatus)
        }
    }
}

@Composable
private fun SyncStatusIcon(status: SyncStatus) {
    val (icon, tint, description) = when (status) {
        SyncStatus.SYNCED -> Triple(
            Icons.Default.CloudSync,
            MaterialTheme.colorScheme.primary,
            "Synced"
        )
        SyncStatus.PENDING -> Triple(
            Icons.Default.Sync,
            MaterialTheme.colorScheme.secondary,
            "Pending sync"
        )
        SyncStatus.FAILED -> Triple(
            Icons.Default.CloudOff,
            MaterialTheme.colorScheme.error,
            "Sync failed"
        )
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LinkOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Share a link from any app to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

private fun formatTimestamp(link: Link): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
    return formatter.format(link.savedAt)
}
