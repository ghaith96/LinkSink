package com.linksink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.linksink.model.DateRange
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import com.linksink.model.Topic
import com.linksink.ui.components.EditTopicSheet
import com.linksink.ui.components.FilterChips
import com.linksink.ui.components.LinkListFiltersSheet
import com.linksink.ui.components.LinkFilterChips
import com.linksink.ui.components.SearchBar
import com.linksink.ui.components.TopicChipSmall
import com.linksink.ui.components.TopicEditPickerSheet
import com.linksink.ui.components.TopicSectionHeader
import com.linksink.viewmodel.LinkFilter
import com.linksink.viewmodel.LinkListUiState
import com.linksink.viewmodel.LinkListViewModel
import com.linksink.viewmodel.TopicSection
import com.linksink.viewmodel.TopicUiState
import com.linksink.viewmodel.TopicViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun shouldShowActiveFilterChips(topicFilter: Long?, dateRange: DateRange?): Boolean =
    topicFilter != null || dateRange != null

internal fun shouldShowLinkFilterChips(hasLinks: Boolean, hasReadLinks: Boolean): Boolean =
    hasLinks && hasReadLinks

internal fun shouldShowPendingSyncBanner(pendingCount: Int): Boolean = pendingCount > 0

internal fun shouldShowSearchField(query: String, isExpanded: Boolean): Boolean =
    isExpanded || query.isNotEmpty()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkListScreen(
    viewModel: LinkListViewModel,
    topicViewModel: TopicViewModel,
    onSettingsClick: () -> Unit,
    onArchivedClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val topicFilter by viewModel.topicFilter.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val sectionStates by viewModel.sectionStates.collectAsState()
    val topicUiState by topicViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showFiltersSheet by remember { mutableStateOf(false) }
    var groupByTopic by rememberSaveable { mutableStateOf(true) }

    LinkListScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        topicFilter = topicFilter,
        dateRange = dateRange,
        topics = topics,
        sectionStates = sectionStates,
        groupByTopic = groupByTopic,
        showFiltersSheet = showFiltersSheet,
        topicUiState = topicUiState,
        context = context,
        onSettingsClick = onSettingsClick,
        onArchivedClick = onArchivedClick,
        onNotificationsClick = onNotificationsClick,
        onSearchQueryChange = viewModel::setSearchQuery,
        onLinkFilterSelected = viewModel::setLinkFilter,
        onOpenFiltersSheet = { showFiltersSheet = true },
        onDismissFiltersSheet = { showFiltersSheet = false },
        onApplyFilters = { state ->
            viewModel.setTopicFilter(state.topicId)
            viewModel.setDateRange(state.dateRange)
            groupByTopic = state.groupByTopic
            showFiltersSheet = false
        },
        onClearTopicFilter = { viewModel.setTopicFilter(null) },
        onClearDateRange = { viewModel.setDateRange(null) },
        onClearAllFilters = viewModel::clearFilters,
        onSyncPendingLinks = viewModel::syncPendingLinks,
        onToggleSection = viewModel::toggleSection,
        onDeleteLink = viewModel::deleteLink,
        onToggleRead = viewModel::toggleReadStatus,
        onArchiveLink = viewModel::archiveLink,
        onOpenLink = viewModel::openLink,
        onRefresh = viewModel::refresh,
        onSelectTopicForEdit = topicViewModel::selectTopic,
        onUpdateTopic = topicViewModel::updateTopic,
        onTestWebhook = topicViewModel::testWebhook,
        onClearTestResult = topicViewModel::clearTestResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkListScreenContent(
    uiState: LinkListUiState,
    searchQuery: String,
    topicFilter: Long?,
    dateRange: DateRange?,
    topics: List<Topic>,
    sectionStates: Map<String, Boolean>,
    groupByTopic: Boolean,
    showFiltersSheet: Boolean,
    topicUiState: TopicUiState,
    context: android.content.Context,
    onSettingsClick: () -> Unit,
    onArchivedClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onSearchQueryChange: (String) -> Unit,
    onLinkFilterSelected: (LinkFilter) -> Unit,
    onOpenFiltersSheet: () -> Unit,
    onDismissFiltersSheet: () -> Unit,
    onApplyFilters: (FilterState) -> Unit,
    onClearTopicFilter: () -> Unit,
    onClearDateRange: () -> Unit,
    onClearAllFilters: () -> Unit,
    onSyncPendingLinks: () -> Unit,
    onToggleSection: (String) -> Unit,
    onDeleteLink: (Link) -> Unit,
    onToggleRead: (Link) -> Unit,
    onArchiveLink: (Link) -> Unit,
    onOpenLink: (Link) -> Unit,
    onRefresh: () -> Unit,
    onSelectTopicForEdit: (Topic?) -> Unit,
    onUpdateTopic: (Topic) -> Unit,
    onTestWebhook: (String) -> Unit,
    onClearTestResult: () -> Unit
) {
    val selectedTopicName = when (topicFilter) {
        null -> null
        -1L -> "Uncategorized"
        else -> topics.find { it.id == topicFilter }?.name
    }
    val appliedFilterState = filterStateFromApplied(topicFilter, dateRange, groupByTopic)
    val pendingCount = (uiState as? LinkListUiState.Success)?.pendingCount ?: 0
    val searchFocusRequester = remember { FocusRequester() }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var showEditTopicPicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            LinkListTopBar(
                overflowVisibility = linkListTopBarOverflowVisibility(
                    pendingCount = pendingCount,
                    archivedAvailable = onArchivedClick != null,
                    notificationsAvailable = onNotificationsClick != null,
                    topicsAvailableForEdit = topics.isNotEmpty()
                ),
                onSearchClick = {
                    searchExpanded = true
                    coroutineScope.launch {
                        delay(50)
                        searchFocusRequester.requestFocus()
                    }
                },
                onOpenFiltersSheet = onOpenFiltersSheet,
                onSettingsClick = onSettingsClick,
                onNotificationsClick = onNotificationsClick,
                onArchivedClick = onArchivedClick,
                onSyncPendingLinks = onSyncPendingLinks,
                onEditTopicsClick = { showEditTopicPicker = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinkListSearchHeader(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    onSearchQueryChange(newQuery)
                    if (newQuery.isEmpty()) {
                        searchExpanded = false
                    }
                },
                searchExpanded = searchExpanded,
                focusRequester = searchFocusRequester
            )

            val successState = uiState as? LinkListUiState.Success
            val linkFilter = successState?.linkFilter ?: LinkFilter.ALL
            
            if (successState != null && shouldShowLinkFilterChips(successState.hasLinks, successState.hasReadLinks)) {
                LinkFilterChips(
                    selectedFilter = linkFilter,
                    onFilterSelected = onLinkFilterSelected
                )
            }

            if (shouldShowActiveFilterChips(topicFilter, dateRange)) {
                FilterChips(
                    topicName = selectedTopicName,
                    dateRange = dateRange,
                    onClearTopic = onClearTopicFilter,
                    onClearDateRange = onClearDateRange,
                    onClearAll = onClearAllFilters
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
                    if (useSectionedLayout(state, groupByTopic)) {
                        TopicSectionedList(
                            sections = state.topicSections,
                            sectionStates = sectionStates,
                            pendingCount = state.pendingCount,
                            onSyncPendingLinks = onSyncPendingLinks,
                            onToggleSection = onToggleSection,
                            onDelete = onDeleteLink,
                            onToggleRead = if (state.linkFilter != LinkFilter.ARCHIVED) {
                                onToggleRead
                            } else null,
                            onArchive = if (state.linkFilter != LinkFilter.ARCHIVED) {
                                onArchiveLink
                            } else null,
                            onOpenLink = onOpenLink,
                            onEditTopic = { topic -> onSelectTopicForEdit(topic) },
                            context = context
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            if (shouldShowPendingSyncBanner(state.pendingCount)) {
                                item(key = "pending_sync_banner") {
                                    PendingSyncBanner(
                                        pendingCount = state.pendingCount,
                                        onSyncNow = onSyncPendingLinks
                                    )
                                }
                            }
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
                                    onDelete = { onDeleteLink(link) },
                                    onToggleRead = if (state.linkFilter != LinkFilter.ARCHIVED) {
                                        { onToggleRead(link) }
                                    } else null,
                                    startToEnd = if (state.linkFilter != LinkFilter.ARCHIVED) {
                                        SwipeStartToEnd(
                                            action = getSwipeAction(isArchived = link.isArchived),
                                            onComplete = { onArchiveLink(link) }
                                        )
                                    } else null,
                                    onClick = {
                                        onOpenLink(link)
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }

                is LinkListUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = onRefresh)
                }
            }
        }

        if (showFiltersSheet) {
            LinkListFiltersSheet(
                applied = appliedFilterState,
                topics = topics,
                onDismiss = onDismissFiltersSheet,
                onApply = onApplyFilters
            )
        }

        if (showEditTopicPicker) {
            TopicEditPickerSheet(
                topics = topics,
                onDismiss = { showEditTopicPicker = false },
                onTopicSelected = { topic ->
                    onSelectTopicForEdit(topic)
                    showEditTopicPicker = false
                }
            )
        }

        topicUiState.selectedTopic?.let { topic ->
            EditTopicSheet(
                topic = topic,
                onDismiss = { onSelectTopicForEdit(null) },
                onSave = onUpdateTopic,
                onTestWebhook = onTestWebhook,
                testResult = topicUiState.testResult,
                testingWebhook = topicUiState.testingWebhook,
                onClearTestResult = onClearTestResult
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopicSectionedList(
    sections: List<TopicSection>,
    sectionStates: Map<String, Boolean>,
    pendingCount: Int,
    onSyncPendingLinks: () -> Unit,
    onToggleSection: (String) -> Unit,
    onDelete: (Link) -> Unit,
    onToggleRead: ((Link) -> Unit)? = null,
    onArchive: ((Link) -> Unit)? = null,
    onOpenLink: (Link) -> Unit,
    onEditTopic: (Topic) -> Unit,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.lg)
    ) {
        if (shouldShowPendingSyncBanner(pendingCount)) {
            item(key = "pending_sync_banner") {
                PendingSyncBanner(
                    pendingCount = pendingCount,
                    onSyncNow = onSyncPendingLinks,
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg)
                        .padding(top = Spacing.sm, bottom = Spacing.xs)
                )
            }
        }
        sections.forEach { section ->
            val key = section.sectionKey
            val expanded = sectionStates[key] ?: true

            stickyHeader(key = "header_$key") {
                Column {
                    TopicSectionHeader(
                        topic = section.topic,
                        linkCount = section.links.size,
                        expanded = expanded,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onToggleSection(key) },
                                onLongClick = section.topic?.let { topic ->
                                    { onEditTopic(topic) }
                                }
                            )
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
                        onToggleRead = onToggleRead?.let { { it(link) } },
                        startToEnd = onArchive?.let { handler ->
                            SwipeStartToEnd(
                                action = getSwipeAction(isArchived = link.isArchived),
                                onComplete = { handler(link) }
                            )
                        },
                        onClick = {
                            onOpenLink(link)
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

enum class LinkStatus {
    UNREAD,
    READ,
    ARCHIVED
}

internal fun linkStatusFromLink(link: Link): LinkStatus =
    linkStatusFromFlags(isRead = link.isRead, isArchived = link.isArchived)

internal fun linkStatusFromFlags(isRead: Boolean, isArchived: Boolean): LinkStatus =
    when {
        isArchived -> LinkStatus.ARCHIVED
        !isRead -> LinkStatus.UNREAD
        else -> LinkStatus.READ
    }

enum class LinkStatusIndicatorIcon {
    UNREAD_DOT,
    READ_CHECK,
    ARCHIVE
}

enum class LinkStatusEmphasis {
    HIGH,
    STANDARD,
    SUBDUED
}

enum class LinkStatusAccentStripe {
    UNREAD,
    READ,
    ARCHIVED
}

data class LinkStatusUiModel(
    val icon: LinkStatusIndicatorIcon,
    val emphasis: LinkStatusEmphasis,
    val accentStripe: LinkStatusAccentStripe,
    val contentDescription: String
) {
    companion object {
        fun fromLinkStatus(status: LinkStatus): LinkStatusUiModel = when (status) {
            LinkStatus.UNREAD -> LinkStatusUiModel(
                icon = LinkStatusIndicatorIcon.UNREAD_DOT,
                emphasis = LinkStatusEmphasis.HIGH,
                accentStripe = LinkStatusAccentStripe.UNREAD,
                contentDescription = "Unread link"
            )
            LinkStatus.READ -> LinkStatusUiModel(
                icon = LinkStatusIndicatorIcon.READ_CHECK,
                emphasis = LinkStatusEmphasis.STANDARD,
                accentStripe = LinkStatusAccentStripe.READ,
                contentDescription = "Read link"
            )
            LinkStatus.ARCHIVED -> LinkStatusUiModel(
                icon = LinkStatusIndicatorIcon.ARCHIVE,
                emphasis = LinkStatusEmphasis.SUBDUED,
                accentStripe = LinkStatusAccentStripe.ARCHIVED,
                contentDescription = "Archived link"
            )
        }
    }
}

@Composable
internal fun LinkStatusIndicator(
    model: LinkStatusUiModel,
    modifier: Modifier = Modifier
) {
    val iconVector = when (model.icon) {
        LinkStatusIndicatorIcon.UNREAD_DOT -> Icons.Filled.FiberManualRecord
        LinkStatusIndicatorIcon.READ_CHECK -> Icons.Filled.CheckCircle
        LinkStatusIndicatorIcon.ARCHIVE -> Icons.Filled.Archive
    }
    val size = when (model.emphasis) {
        LinkStatusEmphasis.HIGH -> 22.dp
        LinkStatusEmphasis.STANDARD -> 20.dp
        LinkStatusEmphasis.SUBDUED -> 18.dp
    }
    val tint = when (model.emphasis) {
        LinkStatusEmphasis.HIGH -> MaterialTheme.colorScheme.primary
        LinkStatusEmphasis.STANDARD -> MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
        LinkStatusEmphasis.SUBDUED -> MaterialTheme.colorScheme.tertiary
    }
    Icon(
        imageVector = iconVector,
        contentDescription = model.contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
private fun linkStatusAccentColor(stripe: LinkStatusAccentStripe): Color = when (stripe) {
    LinkStatusAccentStripe.UNREAD -> MaterialTheme.colorScheme.primary
    LinkStatusAccentStripe.READ -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    LinkStatusAccentStripe.ARCHIVED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
}

@Composable
internal fun LinkListSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    searchExpanded: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val visible = shouldShowSearchField(query, searchExpanded)
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.focusRequester(focusRequester)
        )
    }
}

/** Pure UI flags for link row chrome (sync icon visibility). Read/archive status uses [LinkStatusIndicator]. */
internal object LinkRowUiFlags {
    fun shouldShowSyncStatusIcon(syncStatus: SyncStatus): Boolean =
        syncStatus != SyncStatus.SYNCED
}

@Composable
internal fun PendingSyncBanner(
    pendingCount: Int,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$pendingCount pending sync",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        TextButton(onClick = onSyncNow) {
            Text("Sync now")
        }
    }
}

internal enum class SwipeStartToEndIconKind {
    ARCHIVE,
    UNARCHIVE
}

internal enum class SwipeStartToEndColorRole {
    TERTIARY_CONTAINER,
    SECONDARY_CONTAINER
}

internal sealed class SwipeAction {
    data class StartToEnd(
        val label: String,
        val iconKind: SwipeStartToEndIconKind,
        val colorRole: SwipeStartToEndColorRole,
        val contentDescription: String = label
    ) : SwipeAction()
}

internal fun getSwipeAction(isArchived: Boolean): SwipeAction.StartToEnd =
    if (isArchived) {
        SwipeAction.StartToEnd(
            label = "Unarchive",
            iconKind = SwipeStartToEndIconKind.UNARCHIVE,
            colorRole = SwipeStartToEndColorRole.SECONDARY_CONTAINER
        )
    } else {
        SwipeAction.StartToEnd(
            label = "Archive",
            iconKind = SwipeStartToEndIconKind.ARCHIVE,
            colorRole = SwipeStartToEndColorRole.TERTIARY_CONTAINER
        )
    }

internal data class SwipeStartToEnd(
    val action: SwipeAction.StartToEnd,
    val onComplete: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeableLinkCard(
    link: Link,
    topicName: String?,
    topicColor: Int?,
    topicEmoji: String? = null,
    onDelete: () -> Unit,
    onToggleRead: (() -> Unit)? = null,
    startToEnd: SwipeStartToEnd? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    startToEnd?.onComplete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            when (direction) {
                SwipeToDismissBoxValue.EndToStart -> SwipeDeleteBackground()
                SwipeToDismissBoxValue.StartToEnd ->
                    startToEnd?.let { SwipeStartToEndBackground(it.action) }
                else -> {}
            }
        },
        enableDismissFromStartToEnd = startToEnd != null
    ) {
        LinkCard(
            link = link,
            topicName = topicName,
            topicColor = topicColor,
            topicEmoji = topicEmoji,
            onToggleRead = onToggleRead,
            onClick = onClick
        )
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
private fun SwipeStartToEndBackground(action: SwipeAction.StartToEnd) {
    val backgroundColor = when (action.colorRole) {
        SwipeStartToEndColorRole.TERTIARY_CONTAINER -> MaterialTheme.colorScheme.tertiaryContainer
        SwipeStartToEndColorRole.SECONDARY_CONTAINER -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (action.colorRole) {
        SwipeStartToEndColorRole.TERTIARY_CONTAINER -> MaterialTheme.colorScheme.onTertiaryContainer
        SwipeStartToEndColorRole.SECONDARY_CONTAINER -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val iconVector = when (action.iconKind) {
        SwipeStartToEndIconKind.ARCHIVE -> Icons.Filled.Archive
        SwipeStartToEndIconKind.UNARCHIVE -> Icons.Filled.Unarchive
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = action.contentDescription,
                tint = contentColor
            )
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
internal fun LinkCard(
    link: Link,
    topicName: String?,
    topicColor: Int?,
    topicEmoji: String? = null,
    onToggleRead: (() -> Unit)? = null,
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
        val status = linkStatusFromLink(link)
        val statusUi = LinkStatusUiModel.fromLinkStatus(status)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(linkStatusAccentColor(statusUi.accentStripe))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(Spacing.md),
                verticalAlignment = Alignment.Top
            ) {
                LinkStatusIndicator(
                    model = statusUi,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                FaviconImage(domain = link.domain)

                Spacer(modifier = Modifier.width(Spacing.md))

                val titleWeight = when (statusUi.emphasis) {
                    LinkStatusEmphasis.HIGH, LinkStatusEmphasis.STANDARD -> FontWeight.SemiBold
                    LinkStatusEmphasis.SUBDUED -> FontWeight.Medium
                }
                val titleColor = when (statusUi.emphasis) {
                    LinkStatusEmphasis.HIGH, LinkStatusEmphasis.STANDARD ->
                        MaterialTheme.colorScheme.onSurface
                    LinkStatusEmphasis.SUBDUED ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                }
                val metaAlpha = when (statusUi.emphasis) {
                    LinkStatusEmphasis.SUBDUED -> 0.85f
                    else -> 1f
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = link.title ?: link.domain,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = titleWeight,
                        color = titleColor,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = metaAlpha),
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
                            color = MaterialTheme.colorScheme.outline.copy(alpha = metaAlpha)
                        )
                        if (topicName != null) {
                            TopicChipSmall(topicName = topicName, color = topicColor, emoji = topicEmoji)
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        if (LinkRowUiFlags.shouldShowSyncStatusIcon(link.syncStatus)) {
                            SyncStatusIcon(link.syncStatus)
                        }
                    }
                }

                if (onToggleRead != null) {
                    IconButton(onClick = onToggleRead) {
                        Icon(
                            imageVector = if (link.isRead) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                            contentDescription = if (link.isRead) "Mark as unread" else "Mark as read",
                            tint = if (link.isRead) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                        )
                    }
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
        SyncStatus.LOCAL_ONLY -> Triple(
            Icons.Default.Lock,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Local only"
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
internal fun EmptyContent(
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
