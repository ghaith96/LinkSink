package com.linksink.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.model.DateRange

enum class DateRangePreset(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateRangePickerSheet(
    currentRange: DateRange?,
    onRangeSelected: (DateRange?) -> Unit,
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
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Filter by Date",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangePreset.entries.forEach { preset ->
                    val presetRange = when (preset) {
                        DateRangePreset.TODAY -> DateRange.today()
                        DateRangePreset.THIS_WEEK -> DateRange.thisWeek()
                        DateRangePreset.THIS_MONTH -> DateRange.thisMonth()
                    }

                    val isSelected = currentRange?.let { range ->
                        range.start == presetRange.start && range.end == presetRange.end
                    } ?: false

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onRangeSelected(presetRange)
                            onDismiss()
                        },
                        label = { Text(preset.label) }
                    )
                }
            }

            if (currentRange != null) {
                TextButton(
                    onClick = {
                        onRangeSelected(null)
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text("Clear Filter")
                }
            }
        }
    }
}
