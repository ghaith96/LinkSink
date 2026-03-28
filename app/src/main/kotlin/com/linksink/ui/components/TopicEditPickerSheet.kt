package com.linksink.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linksink.model.Topic
import com.linksink.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicEditPickerSheet(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onTopicSelected: (Topic) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = "Edit topic",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            topics.forEach { topic ->
                TextButton(
                    onClick = { onTopicSelected(topic) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = topic.name)
                }
            }
        }
    }
}
