package com.linksink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.linksink.ui.theme.ComponentSize
import com.linksink.ui.theme.Spacing
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import com.linksink.ui.components.DateRangePickerSheet
import com.linksink.ui.components.EditTopicSheet
import com.linksink.ui.components.FilterChips
import com.linksink.ui.components.SearchBar
import com.linksink.ui.components.TopicChipSmall
import com.linksink.ui.components.TopicSectionHeader
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.LinkListViewModel
import com.linksink.viewmodel.TopicSection
import com.linksink.viewmodel.TopicViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkListScreen(
    viewModel: LinkListViewModel,
    topicViewModel: TopicViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val topicFilter by viewModel.topicFilter.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val sectionStates by viewModel.sectionStates.collectAsState()
    val topicUiState by topicViewModel.uiState.collectAsState()
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
                    if (useSectionedLayout(state)) {
                        TopicSectionedList(
                            sections = state.topicSections,
                            sectionStates = sectionStates,
                            onToggleSection = { viewModel.toggleSection(it) },
                            onDelete = { viewModel.deleteLink(it) },
                            onEditTopic = { topic -> topicViewModel.selectTopic(topic) },
                            context = context
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(
                                items = state.links,
                                key = { it.id }
                            ) { link ->
                                val linkTopic = topics.find { it.id == link.topicId }
                                SwipeableLinkCard(
                                    link = link,
                                    topicName = linkTopic?.name,
                                    topicColor = linkTopic?.color,
                                    topicEmoji = linkTopic?.emoji,
                                    onDelete = { viewModel.deleteLink(link) },
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }

                is LinkListUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = { viewModel.refresh() })
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

        // Edit sheet for inline CUSTOM webhook mode selection
        topicUiState.selectedTopic?.let { topic ->
            EditTopicSheet(
                topic = topic,
                onDismiss = { topicViewModel.selectTopic(null) },
                onSave = { updatedTopic ->
                    topicViewModel.updateTopic(updatedTopic)
                },
                onTestWebhook = topicViewModel::testWebhook,
                testResult = topicUiState.testResult,
                testingWebhook = topicUiState.testingWebhook,
                onClearTestResult = topicViewModel::clearTestResult
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopicSectionedList(
    sections: List<TopicSection>,
    sectionStates: Map<String, Boolean>,
    onToggleSection: (String) -> Unit,
    onDelete: (Link) -> Unit,
    onEditTopic: (Topic) -> Unit,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.lg)
    ) {
        sections.forEach { section ->
            val key = section.sectionKey
            val expanded = sectionStates[key] ?: true

            stickyHeader(key = "header_$key") {
                Column {
                    TopicSectionHeader(
                        topic = section.topic,
                        linkCount = section.links.size,
                        expanded = expanded,
                        onToggle = { onToggleSection(key) },
                        onEditTopic = { section.topic?.let { onEditTopic(it) } },
                        modifier = Modifier
                    )
                    HorizontalDivider()
                }
            }

            items(
                items = section.links,
                key = { "link_${it.id}" }
            ) { link ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    SwipeableLinkCard(
                        link = link,
                        topicName = section.topic?.name,
                        topicColor = section.topic?.color,
                        topicEmoji = section.topic?.emoji,
                        onDelete = { onDelete(link) },
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                }
            }
        }
    }
}

internal fun useSectionedLayout(state: LinkListUiState.Success): Boolean =
    state.topicSections.isNotEmpty() && !state.hasActiveFilters

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
    topicColor: Int?,
    topicEmoji: String? = null,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier,
        backgroundContent = {
            SwipeDeleteBackground()
        },
        enableDismissFromStartToEnd = false
    ) {
        LinkCard(link = link, topicName = topicName, topicColor = topicColor, topicEmoji = topicEmoji, onClick = onClick)
    }
}

@Composable
private fun SwipeDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(end = Spacing.xl)
        )
    }
}

@Composable
internal fun LinkCard(
    link: Link,
    topicName: String?,
    topicColor: Int?,
    topicEmoji: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // elevation, not spacing
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            FaviconImage(domain = link.domain)

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title ?: link.domain,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                DomainPill(domain = link.domain)

                if (!link.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = link.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = formatTimestamp(link),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (topicName != null) {
                        TopicChipSmall(topicName = topicName, color = topicColor, emoji = topicEmoji)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    SyncStatusIcon(link.syncStatus)
                }
            }
        }
    }
}

@Composable
private fun FaviconImage(domain: String) {
    Box(
        modifier = Modifier
            .size(ComponentSize.FaviconOuter)
            .clip(RoundedCornerShape(ComponentSize.FaviconCorner))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = faviconUrl(domain),
            contentDescription = null,
            modifier = Modifier
                .size(ComponentSize.FaviconInner)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = {},
            onLoading = {}
        )
        // Letter fallback shown behind AsyncImage (visible when image fails or loading)
        Text(
            text = domain.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DomainPill(domain: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Spacing.xs))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Text(
            text = domain,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal fun faviconUrl(domain: String): String =
    "https://www.google.com/s2/favicons?sz=64&domain=$domain"

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
        modifier = Modifier.size(ComponentSize.SyncIcon)
    )
}

@Composable
private fun StateIllustration(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(ComponentSize.IllustrationCircle)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ComponentSize.IllustrationIcon),
            tint = iconTint
        )
    }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            StateIllustration(
                icon = Icons.Default.LinkOff,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Share a link from any app to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            StateIllustration(
                icon = Icons.Default.CloudOff,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onRetry != null) {
                androidx.compose.material3.Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

private fun formatTimestamp(link: Link): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
    return formatter.format(link.savedAt)
}

// --- Compose Previews ---

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun PreviewLinkCard() {
    com.linksink.ui.theme.LinkSinkTheme {
        androidx.compose.material3.Surface {
            LinkCard(
                link = Link(
                    id = 1,
                    url = "https://github.com/google/compose-samples",
                    title = "Jetpack Compose samples by Google",
                    description = "A collection of sample apps demonstrating how to build Android apps with Jetpack Compose.",
                    domain = "github.com",
                    topicId = null,
                    savedAt = java.time.Instant.now(),
                    syncStatus = com.linksink.model.SyncStatus.SYNCED
                ),
                topicName = "Dev",
                topicColor = 0xFF5865F2.toInt(),
                onClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun PreviewEmptyContent() {
    com.linksink.ui.theme.LinkSinkTheme {
        androidx.compose.material3.Surface {
            EmptyContent(message = "No links saved yet")
        }
    }
}

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun PreviewErrorContent() {
    com.linksink.ui.theme.LinkSinkTheme {
        androidx.compose.material3.Surface {
            ErrorContent(message = "Failed to load links. Check your connection.", onRetry = {})
        }
    }
}
