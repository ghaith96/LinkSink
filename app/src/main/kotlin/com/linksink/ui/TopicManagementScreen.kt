package com.linksink.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.HookMode
import com.linksink.model.Topic
import com.linksink.viewmodel.TopicViewModel
import com.linksink.viewmodel.WebhookTestResult

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
        if (topics.isEmpty()) {
            EmptyTopicsContent(
                modifier = Modifier.padding(padding),
                onCreateClick = { showCreateSheet = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topics, key = { it.id }) { topic ->
                    TopicCard(
                        topic = topic,
                        onEdit = { viewModel.selectTopic(topic) },
                        onDelete = { viewModel.requestDeleteTopic(topic.id) }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateTopicSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, hookMode, customUrl ->
                viewModel.createTopic(name, hookMode, customUrl)
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create topics to organize your links",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Topic")
        }
    }
}

@Composable
private fun TopicCard(
    topic: Topic,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
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
    onCreate: (name: String, hookMode: HookMode, customUrl: String?) -> Unit,
    onTestWebhook: (String) -> Unit,
    testResult: WebhookTestResult?,
    testingWebhook: Boolean,
    onClearTestResult: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hookMode by remember { mutableStateOf(HookMode.USE_GLOBAL) }
    var customUrl by remember { mutableStateOf("") }

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
            onTestWebhook = onTestWebhook,
            testResult = testResult,
            testingWebhook = testingWebhook,
            onClearTestResult = onClearTestResult,
            onSave = {
                if (name.isNotBlank()) {
                    onCreate(name, hookMode, customUrl.takeIf { hookMode == HookMode.CUSTOM && it.isNotBlank() })
                }
            },
            onCancel = onDismiss,
            saveButtonText = "Create"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTopicSheet(
    topic: Topic,
    onDismiss: () -> Unit,
    onSave: (Topic) -> Unit,
    onTestWebhook: (String) -> Unit,
    testResult: WebhookTestResult?,
    testingWebhook: Boolean,
    onClearTestResult: () -> Unit
) {
    var name by remember(topic) { mutableStateOf(topic.name) }
    var hookMode by remember(topic) { mutableStateOf(topic.hookMode) }
    var customUrl by remember(topic) { mutableStateOf(topic.customWebhookUrl ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        TopicForm(
            title = "Edit Topic",
            name = name,
            onNameChange = { name = it },
            hookMode = hookMode,
            onHookModeChange = { hookMode = it },
            customUrl = customUrl,
            onCustomUrlChange = { customUrl = it },
            onTestWebhook = onTestWebhook,
            testResult = testResult,
            testingWebhook = testingWebhook,
            onClearTestResult = onClearTestResult,
            onSave = {
                if (name.isNotBlank()) {
                    onSave(
                        topic.copy(
                            name = name,
                            hookMode = hookMode,
                            customWebhookUrl = customUrl.takeIf { hookMode == HookMode.CUSTOM && it.isNotBlank() }
                        )
                    )
                }
            },
            onCancel = onDismiss,
            saveButtonText = "Save"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicForm(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    hookMode: HookMode,
    onHookModeChange: (HookMode) -> Unit,
    customUrl: String,
    onCustomUrlChange: (String) -> Unit,
    onTestWebhook: (String) -> Unit,
    testResult: WebhookTestResult?,
    testingWebhook: Boolean,
    onClearTestResult: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    saveButtonText: String
) {
    var hookModeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Topic Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = hookModeExpanded,
            onExpandedChange = { hookModeExpanded = it }
        ) {
            OutlinedTextField(
                value = when (hookMode) {
                    HookMode.LOCAL_ONLY -> "Local Only"
                    HookMode.USE_GLOBAL -> "Use Global Webhook"
                    HookMode.CUSTOM -> "Custom Webhook"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Webhook Mode") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hookModeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = hookModeExpanded,
                onDismissRequest = { hookModeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Local Only") },
                    onClick = {
                        onHookModeChange(HookMode.LOCAL_ONLY)
                        hookModeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Use Global Webhook") },
                    onClick = {
                        onHookModeChange(HookMode.USE_GLOBAL)
                        hookModeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Custom Webhook") },
                    onClick = {
                        onHookModeChange(HookMode.CUSTOM)
                        hookModeExpanded = false
                    }
                )
            }
        }

        if (hookMode == HookMode.CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customUrl,
                onValueChange = {
                    onCustomUrlChange(it)
                    onClearTestResult()
                },
                label = { Text("Webhook URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://discord.com/api/webhooks/...") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onTestWebhook(customUrl) },
                    enabled = customUrl.isNotBlank() && !testingWebhook
                ) {
                    if (testingWebhook) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Test Webhook")
                }

                Spacer(modifier = Modifier.width(8.dp))

                when (testResult) {
                    is WebhookTestResult.Success -> {
                        Text(
                            text = "Success!",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is WebhookTestResult.Failure -> {
                        Text(
                            text = "Failed: ${testResult.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    null -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && (hookMode != HookMode.CUSTOM || customUrl.isNotBlank())
            ) {
                Text(saveButtonText)
            }
        }
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
                    Spacer(modifier = Modifier.height(8.dp))
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
