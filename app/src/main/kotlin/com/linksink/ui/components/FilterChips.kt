package com.linksink.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.DateRange
import com.linksink.ui.theme.Spacing
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@Composable
fun FilterChips(
    topicName: String?,
    dateRange: DateRange?,
    onClearTopic: () -> Unit,
    onClearDateRange: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMultipleFilters = topicName != null && dateRange != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (topicName != null) {
            InputChip(
                selected = true,
                onClick = onClearTopic,
                label = { Text(topicName) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear topic filter",
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        if (dateRange != null) {
            InputChip(
                selected = true,
                onClick = onClearDateRange,
                label = { Text(formatDateRange(dateRange)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear date filter",
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        if (hasMultipleFilters) {
            AssistChip(
                onClick = onClearAll,
                label = { Text("Clear All") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    }
}

private fun formatDateRange(dateRange: DateRange): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
        .withZone(ZoneId.systemDefault())
    val startStr = formatter.format(dateRange.start)
    val endStr = formatter.format(dateRange.end)
    return "$startStr - $endStr"
}
