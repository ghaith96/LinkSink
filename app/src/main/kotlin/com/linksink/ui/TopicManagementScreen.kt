package com.linksink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linksink.model.HookMode
import com.linksink.model.Topic
import com.linksink.model.displayName
import com.linksink.ui.components.EditTopicSheet
import com.linksink.ui.components.TopicForm
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.TopicViewModel
import com.linksink.viewmodel.WebhookTestResult
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicManagementScreen(
    viewModel: TopicViewModel,
    onBack: () -> Unit
) {
    val topics by viewModel.topics.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val orderedTopics = remember { mutableStateListOf<Topic>() }
    LaunchedEffect(topics) {
        orderedTopics.clear()
        orderedTopics.addAll(topics)
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = orderedTopics.indexOfFirst { it.id == from.key }
        val toIndex = orderedTopics.indexOfFirst { it.id == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            orderedTopics.add(toIndex, orderedTopics.removeAt(fromIndex))
            viewModel.updateTopicOrder(orderedTopics.map { it.id })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Topics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create topic")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (orderedTopics.isEmpty()) {
            EmptyTopicsContent(
                modifier = Modifier.padding(padding),
                onCreateClick = { showCreateSheet = true }
            )
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(orderedTopics, key = { it.id }) { topic ->
                    ReorderableItem(reorderableState, key = topic.id) {
                        TopicCard(
                            topic = topic,
                            onEdit = { viewModel.selectTopic(topic) },
                            onDelete = { viewModel.requestDeleteTopic(topic.id) },
                            dragHandleModifier = Modifier.draggableHandle()
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateTopicSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, hookMode, customUrl, color, emoji ->
                viewModel.createTopic(name, hookMode, customUrl, color, emoji)
                showCreateSheet = false
            },
            onTestWebhook = viewModel::testWebhook,
            testResult = uiState.testResult,
            testingWebhook = uiState.testingWebhook,
            onClearTestResult = viewModel::clearTestResult
        )
    }

    uiState.selectedTopic?.let { topic ->
        EditTopicSheet(
            topic = topic,
            onDismiss = { viewModel.selectTopic(null) },
            onSave = { updatedTopic ->
                viewModel.updateTopic(updatedTopic)
            },
            onTestWebhook = viewModel::testWebhook,
            testResult = uiState.testResult,
            testingWebhook = uiState.testingWebhook,
            onClearTestResult = viewModel::clearTestResult
        )
    }

    uiState.deleteConfirmation?.let { confirmation ->
        DeleteTopicDialog(
            topicName = confirmation.topicName,
            linkCount = confirmation.linkCount,
            onConfirm = { deleteLinks ->
                viewModel.confirmDeleteTopic(deleteLinks)
            },
            onDismiss = { viewModel.cancelDeleteTopic() }
        )
    }
}

@Composable
private fun EmptyTopicsContent(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No topics yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Create topics to organize your links",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Create Topic")
        }
    }
}

@Composable
private fun TopicCard(
    topic: Topic,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // elevation, not spacing
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier.size(Spacing.lg)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            topic.color?.let { colorInt ->
                Box(
                    modifier = Modifier
                        .size(Spacing.md)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.displayName(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = when (topic.hookMode) {
                        HookMode.LOCAL_ONLY -> "Local only (no Discord sync)"
                        HookMode.USE_GLOBAL -> "Uses global webhook"
                        HookMode.CUSTOM -> "Custom webhook"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTopicSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, hookMode: HookMode, customUrl: String?, color: Int?, emoji: String?) -> Unit,
    onTestWebhook: (String) -> Unit,
    testResult: WebhookTestResult?,
    testingWebhook: Boolean,
    onClearTestResult: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hookMode by remember { mutableStateOf(HookMode.USE_GLOBAL) }
    var customUrl by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
    var emoji by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        TopicForm(
            title = "Create Topic",
            name = name,
            onNameChange = { name = it },
            hookMode = hookMode,
            onHookModeChange = { hookMode = it },
            customUrl = customUrl,
            onCustomUrlChange = { customUrl = it },
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            emoji = emoji,
            onEmojiChange = { emoji = it },
            onTestWebhook = onTestWebhook,
            testResult = testResult,
            testingWebhook = testingWebhook,
            onClearTestResult = onClearTestResult,
            onSave = {
                if (name.isNotBlank()) {
                    onCreate(
                        name,
                        hookMode,
                        customUrl.takeIf { hookMode == HookMode.CUSTOM && it.isNotBlank() },
                        selectedColor,
                        emoji.takeIf { it.isNotBlank() }
                    )
                }
            },
            onCancel = onDismiss,
            saveButtonText = "Create"
        )
    }
}

@Composable
private fun DeleteTopicDialog(
    topicName: String,
    linkCount: Int,
    onConfirm: (deleteLinks: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Topic") },
        text = {
            Column {
                Text("Delete \"$topicName\"?")
                if (linkCount > 0) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "This topic has $linkCount link${if (linkCount > 1) "s" else ""}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (linkCount > 0) {
                Column {
                    TextButton(onClick = { onConfirm(false) }) {
                        Text("Move Links to Uncategorized")
                    }
                    TextButton(onClick = { onConfirm(true) }) {
                        Text("Delete Links Too", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Button(onClick = { onConfirm(false) }) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
