package com.linksink.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.Topic
import com.linksink.model.displayName
import com.linksink.ui.components.TopicChip
import com.linksink.ui.theme.ComponentSize
import com.linksink.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopicPickerSheet(
    recentTopics: List<Topic>,
    allTopics: List<Topic>,
    selectedTopicId: Long?,
    onTopicSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xxl)
        ) {
            Text(
                text = "Select Topic",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            )

            ListItem(
                headlineContent = { Text("None (Uncategorized)") },
                trailingContent = {
                    if (selectedTopicId == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable { onTopicSelected(null) }
            )

            if (recentTopics.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    recentTopics.forEach { topic ->
                        TopicChip(
                            topic = topic,
                            isSelected = topic.id == selectedTopicId,
                            onClick = { onTopicSelected(topic.id) }
                        )
                    }
                }
            }

            val otherTopics = allTopics.filterNot { topic ->
                recentTopics.any { it.id == topic.id }
            }

            if (otherTopics.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                Text(
                    text = "All Topics",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )

                LazyColumn(
                    modifier = Modifier.height(ComponentSize.TopicListFixedHeight)
                ) {
                    items(otherTopics) { topic ->
                        ListItem(
                            headlineContent = { Text(topic.displayName()) },
                            trailingContent = {
                                if (topic.id == selectedTopicId) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onTopicSelected(topic.id) }
                        )
                    }
                }
            }
        }
    }
}
