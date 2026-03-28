package com.linksink.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linksink.model.Topic
import com.linksink.ui.theme.ComponentSize
import com.linksink.ui.theme.Spacing

@Composable
internal fun TopicSectionHeader(
    topic: Topic?,
    linkCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEditTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "chevron_rotation"
    )

    val accentColor = topic?.color?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = Spacing.md, end = Spacing.xs, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .width(ComponentSize.TopicColorBarHeight)
                .height(ComponentSize.TopicColorBarWidth)
                .clip(RoundedCornerShape(ComponentSize.TopicColorBarCorner))
                .background(accentColor)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse section" else "Expand section",
            modifier = Modifier.rotate(chevronRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = topic?.name ?: "Uncategorized",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "$linkCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (topic != null) {
            IconButton(
                onClick = onEditTopic,
                modifier = Modifier.size(ComponentSize.AvatarSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit topic settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(ComponentSize.ChevronSize)
                )
            }
        }
    }
}

// --- Compose Previews ---

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun PreviewTopicSectionHeaderExpanded() {
    com.linksink.ui.theme.LinkSinkTheme {
        androidx.compose.material3.Surface {
            TopicSectionHeader(
                topic = com.linksink.model.Topic(
                    id = 1,
                    name = "Design",
                    color = 0xFF5865F2.toInt()
                ),
                linkCount = 12,
                expanded = true,
                onToggle = {},
                onEditTopic = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun PreviewTopicSectionHeaderCollapsed() {
    com.linksink.ui.theme.LinkSinkTheme {
        androidx.compose.material3.Surface {
            TopicSectionHeader(
                topic = com.linksink.model.Topic(id = 2, name = "Reading"),
                linkCount = 5,
                expanded = false,
                onToggle = {},
                onEditTopic = {}
            )
        }
    }
}
