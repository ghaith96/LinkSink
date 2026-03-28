package com.linksink.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.DateRange
import com.linksink.model.Topic
import com.linksink.ui.FilterState
import com.linksink.ui.theme.Spacing
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val TOPIC_UNCATEGORIZED = -1L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkListFiltersSheet(
    applied: FilterState,
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(applied) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(
        applied.topicId,
        applied.dateRange?.start,
        applied.dateRange?.end,
        applied.groupByTopic
    ) {
        draft = applied
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Text(
                text = "Topic",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            TopicOptionRow(
                label = "All topics",
                selected = draft.topicId == null,
                onSelect = { draft = draft.copy(topicId = null) }
            )
            TopicOptionRow(
                label = "Uncategorized",
                selected = draft.topicId == TOPIC_UNCATEGORIZED,
                onSelect = { draft = draft.copy(topicId = TOPIC_UNCATEGORIZED) }
            )
            topics.forEach { topic ->
                TopicOptionRow(
                    label = topic.name,
                    selected = draft.topicId == topic.id,
                    onSelect = { draft = draft.copy(topicId = topic.id) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))

            Text(
                text = "Date range",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateRangeSummary(draft.dateRange),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Tap to choose a preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Group by topic",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Sections when browsing all links",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = draft.groupByTopic,
                    onCheckedChange = { draft = draft.copy(groupByTopic = it) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { draft = draft.clearAll() }) {
                    Text("Clear all")
                }
                Button(
                    onClick = {
                        onApply(draft)
                    }
                ) {
                    Text("Apply")
                }
            }
        }
    }

    if (showDatePicker) {
        DateRangePickerSheet(
            currentRange = draft.dateRange,
            onRangeSelected = {
                draft = draft.copy(dateRange = it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun TopicOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun dateRangeSummary(range: DateRange?): String {
    if (range == null) return "Any time"
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
    val startStr = formatter.format(range.start)
    val endStr = formatter.format(range.end)
    return "$startStr – $endStr"
}
