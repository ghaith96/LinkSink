package com.linksink.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import com.linksink.model.Topic
import com.linksink.model.displayName
import com.linksink.ui.theme.ComponentSize
import com.linksink.ui.theme.Spacing

/** Default presentation for topic section headers (expand/collapse only; edit uses progressive disclosure). */
internal data class TopicSectionHeaderUiConfig(
    val includeVisibleSettingsAction: Boolean
) {
    companion object {
        val Default = TopicSectionHeaderUiConfig(includeVisibleSettingsAction = false)
    }
}

internal fun topicSectionHeaderDefaultUiConfig(): TopicSectionHeaderUiConfig = TopicSectionHeaderUiConfig.Default

@Composable
internal fun TopicSectionHeader(
    topic: Topic?,
    linkCount: Int,
    expanded: Boolean,
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
            text = topic?.displayName() ?: "Uncategorized",
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
