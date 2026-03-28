package com.linksink.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linksink.model.HookMode
import com.linksink.model.Topic

internal fun hookModeLabel(mode: HookMode): String = when (mode) {
    HookMode.LOCAL_ONLY -> "Local Only"
    HookMode.USE_GLOBAL -> "Global"
    HookMode.CUSTOM -> "Custom"
}

@Composable
internal fun TopicSectionHeader(
    topic: Topic?,
    linkCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onHookModeChange: (HookMode) -> Unit,
    onEditCustom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "chevron_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse section" else "Expand section",
            modifier = Modifier.rotate(chevronRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = topic?.name ?: "Uncategorized",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "($linkCount)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (topic != null) {
            SyncModeBadge(
                topic = topic,
                onHookModeChange = onHookModeChange,
                onEditCustom = onEditCustom
            )
        }
    }
}

@Composable
private fun SyncModeBadge(
    topic: Topic,
    onHookModeChange: (HookMode) -> Unit,
    onEditCustom: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    SuggestionChip(
        onClick = { menuExpanded = true },
        label = {
            Text(
                text = hookModeLabel(topic.hookMode),
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.semantics {
            contentDescription = "Sync mode: ${hookModeLabel(topic.hookMode)}"
        }
    )

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        HookMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = hookModeLabel(mode),
                        fontWeight = if (mode == topic.hookMode) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    menuExpanded = false
                    if (mode == HookMode.CUSTOM) {
                        onEditCustom()
                    } else {
                        onHookModeChange(mode)
                    }
                }
            )
        }
    }
}
