package com.linksink.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.LinkFilter

@Composable
fun LinkFilterChips(
    selectedFilter: LinkFilter,
    onFilterSelected: (LinkFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        FilterChip(
            selected = selectedFilter == LinkFilter.ALL,
            onClick = { onFilterSelected(LinkFilter.ALL) },
            label = { Text("All") }
        )
        
        FilterChip(
            selected = selectedFilter == LinkFilter.UNREAD,
            onClick = { onFilterSelected(LinkFilter.UNREAD) },
            label = { Text("Unread") }
        )
        
        FilterChip(
            selected = selectedFilter == LinkFilter.ARCHIVED,
            onClick = { onFilterSelected(LinkFilter.ARCHIVED) },
            label = { Text("Archived") }
        )
    }
}
