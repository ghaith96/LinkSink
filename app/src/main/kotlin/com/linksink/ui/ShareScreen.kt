package com.linksink.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linksink.model.Topic
import com.linksink.ui.components.TopicChip
import com.linksink.viewmodel.ShareUiState
import com.linksink.viewmodel.ShareViewModel
import kotlinx.coroutines.delay

@Composable
fun ShareScreen(
    viewModel: ShareViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentTopics by viewModel.recentTopics.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    var showTopicPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ShareUiState.Success) {
            delay(800)
            onSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is ShareUiState.Idle,
                is ShareUiState.Extracting -> LoadingContent()

                is ShareUiState.Ready -> ReadyContent(
                    url = state.url,
                    domain = state.domain,
                    selectedTopicId = state.selectedTopicId,
                    recentTopics = recentTopics,
                    allTopics = allTopics,
                    onTopicSelected = viewModel::selectTopic,
                    onShowAllTopics = { showTopicPicker = true },
                    onSave = viewModel::saveLink,
                    onCancel = onDismiss
                )

                is ShareUiState.Saving -> SavingContent()

                is ShareUiState.Success -> SuccessContent()

                is ShareUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    onDismiss = onDismiss
                )

                is ShareUiState.NoUrl -> NoUrlContent(onDismiss = onDismiss)
            }
        }
    }

    if (showTopicPicker) {
        TopicPickerSheet(
            recentTopics = recentTopics,
            allTopics = allTopics,
            selectedTopicId = (uiState as? ShareUiState.Ready)?.selectedTopicId,
            onTopicSelected = { topicId ->
                viewModel.selectTopic(topicId)
                showTopicPicker = false
            },
            onDismiss = { showTopicPicker = false }
        )
    }
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(16.dp))
    Text("Processing...")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadyContent(
    url: String,
    domain: String,
    selectedTopicId: Long?,
    recentTopics: List<Topic>,
    allTopics: List<Topic>,
    onTopicSelected: (Long?) -> Unit,
    onShowAllTopics: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val selectedTopicName = allTopics.find { it.id == selectedTopicId }?.name

    Icon(
        imageVector = Icons.Default.Link,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Save Link",
        style = MaterialTheme.typography.titleLarge
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = domain,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTopicName != null) "Topic: $selectedTopicName" else "Select Topic (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (allTopics.isNotEmpty()) {
                TextButton(onClick = onShowAllTopics) {
                    Text("All")
                }
            }
        }

        if (recentTopics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicChip(
                    topic = Topic(id = -1, name = "None"),
                    isSelected = selectedTopicId == null,
                    onClick = { onTopicSelected(null) }
                )
                recentTopics.take(4).forEach { topic ->
                    TopicChip(
                        topic = topic,
                        isSelected = topic.id == selectedTopicId,
                        onClick = { onTopicSelected(topic.id) }
                    )
                }
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
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cancel")
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save")
        }
    }
}

@Composable
private fun SavingContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(16.dp))
    Text("Saving...")
}

@Composable
private fun SuccessContent() {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Saved!",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Error",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.weight(1f)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun NoUrlContent(onDismiss: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "No link found",
        style = MaterialTheme.typography.titleLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "The shared content doesn't contain a valid URL.",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = onDismiss) {
        Text("OK")
    }
}
