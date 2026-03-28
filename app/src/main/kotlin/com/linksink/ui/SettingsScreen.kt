package com.linksink.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linksink.ui.theme.ComponentSize
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.SettingsUiState
import com.linksink.viewmodel.SettingsViewModel
import com.linksink.viewmodel.TestResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onSettingsSaved: () -> Unit,
    onManageTopicsClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSettingsSaved()
        }
    }

    LaunchedEffect(uiState.testResult) {
        when (val result = uiState.testResult) {
            is TestResult.Success -> {
                snackbarHostState.showSnackbar("Connection successful!")
                viewModel.dismissTestResult()
            }
            is TestResult.Failure -> {
                snackbarHostState.showSnackbar("Connection failed: ${result.message}")
                viewModel.dismissTestResult()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinkSink Setup") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                uiState = uiState,
                onWebhookUrlChanged = viewModel::onWebhookUrlChanged,
                onPasteClicked = {
                    clipboardManager.getText()?.text?.let { text ->
                        viewModel.onWebhookUrlChanged(text)
                    }
                },
                onClearClicked = viewModel::clearSettings,
                onTestClicked = viewModel::testConnection,
                onSaveClicked = viewModel::saveSettings,
                onManageTopicsClick = onManageTopicsClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onWebhookUrlChanged: (String) -> Unit,
    onPasteClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onTestClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onManageTopicsClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                Text(
                    text = "How to get a Discord Webhook URL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "1. Open Discord and go to your server\n" +
                            "2. Right-click a channel → Edit Channel\n" +
                            "3. Go to Integrations → Webhooks\n" +
                            "4. Click 'New Webhook' and copy the URL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        OutlinedTextField(
            value = uiState.webhookUrl,
            onValueChange = onWebhookUrlChanged,
            label = { Text("Discord Webhook URL") },
            placeholder = { Text("https://discord.com/api/webhooks/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            isError = uiState.webhookUrl.isNotEmpty() && !uiState.isValidUrl,
            supportingText = {
                if (uiState.webhookUrl.isNotEmpty() && !uiState.isValidUrl) {
                    Text("Invalid Discord webhook URL format")
                }
            },
            trailingIcon = {
                Row {
                    IconButton(onClick = onPasteClicked) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste"
                        )
                    }
                    if (uiState.webhookUrl.isNotEmpty()) {
                        IconButton(onClick = onClearClicked) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onTestClicked,
                enabled = uiState.isValidUrl && !uiState.isTesting,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(ComponentSize.ProgressIndicator)
                            .width(ComponentSize.ProgressIndicator),
                        strokeWidth = ComponentSize.ProgressStrokeWidth
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text("Test Connection")
            }

            Button(
                onClick = onSaveClicked,
                enabled = uiState.isValidUrl && !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(ComponentSize.ProgressIndicator)
                            .width(ComponentSize.ProgressIndicator),
                        strokeWidth = ComponentSize.ProgressStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Save")
            }
        }

        if (onManageTopicsClick != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg)
                ) {
                    Text(
                        text = "Topics",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "Organize your links into topics with custom webhook settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    OutlinedButton(
                        onClick = onManageTopicsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage Topics")
                    }
                }
            }
        }
    }
}
