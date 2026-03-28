package com.linksink.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class TopBarPrimaryAction {
    Search,
    Filters,
    Overflow
}

internal fun linkListTopBarPrimaryActionSlots(): Set<TopBarPrimaryAction> =
    TopBarPrimaryAction.entries.toSet()

data class LinkListTopBarOverflowVisibility(
    val showSettings: Boolean,
    val showNotificationSettings: Boolean,
    val showArchivedLinks: Boolean,
    val showSyncNow: Boolean,
    val showEditTopics: Boolean
)

internal fun linkListTopBarOverflowVisibility(
    pendingCount: Int,
    archivedAvailable: Boolean,
    notificationsAvailable: Boolean,
    topicsAvailableForEdit: Boolean = true
): LinkListTopBarOverflowVisibility = LinkListTopBarOverflowVisibility(
    showSettings = true,
    showNotificationSettings = notificationsAvailable,
    showArchivedLinks = archivedAvailable,
    showSyncNow = shouldShowPendingSyncBanner(pendingCount),
    showEditTopics = topicsAvailableForEdit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkListTopBar(
    overflowVisibility: LinkListTopBarOverflowVisibility,
    onSearchClick: () -> Unit,
    onOpenFiltersSheet: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationsClick: (() -> Unit)?,
    onArchivedClick: (() -> Unit)?,
    onSyncPendingLinks: () -> Unit,
    onEditTopicsClick: () -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("LinkSink") },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }

            IconButton(onClick = onOpenFiltersSheet) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters"
                )
            }

            Box {
                IconButton(onClick = { showOverflowMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    if (overflowVisibility.showSettings) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showOverflowMenu = false
                                onSettingsClick()
                            }
                        )
                    }
                    if (overflowVisibility.showEditTopics) {
                        DropdownMenuItem(
                            text = { Text("Edit topics…") },
                            onClick = {
                                showOverflowMenu = false
                                onEditTopicsClick()
                            }
                        )
                    }
                    if (overflowVisibility.showNotificationSettings) {
                        DropdownMenuItem(
                            text = { Text("Notification settings") },
                            onClick = {
                                showOverflowMenu = false
                                onNotificationsClick?.invoke()
                            }
                        )
                    }
                    if (overflowVisibility.showArchivedLinks) {
                        DropdownMenuItem(
                            text = { Text("Archived links") },
                            onClick = {
                                showOverflowMenu = false
                                onArchivedClick?.invoke()
                            }
                        )
                    }
                    if (overflowVisibility.showSyncNow) {
                        DropdownMenuItem(
                            text = { Text("Sync now") },
                            onClick = {
                                showOverflowMenu = false
                                onSyncPendingLinks()
                            }
                        )
                    }
                }
            }
        }
    )
}
