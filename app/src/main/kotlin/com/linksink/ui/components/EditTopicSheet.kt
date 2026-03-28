package com.linksink.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.HookMode
import com.linksink.model.Topic
import com.linksink.viewmodel.WebhookTestResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditTopicSheet(
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
internal fun TopicForm(
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
