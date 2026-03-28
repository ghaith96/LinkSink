package com.linksink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linksink.ui.theme.Spacing

val PREDEFINED_TOPIC_COLORS: List<Int> = listOf(
    0xFF5865F2.toInt(), // Discord blurple
    0xFF57F287.toInt(), // Discord green
    0xFFFEE75C.toInt(), // Discord yellow
    0xFFEB459E.toInt(), // Discord fuchsia
    0xFFED4245.toInt(), // Discord red
    0xFF3498DB.toInt(), // Blue
    0xFFE67E22.toInt(), // Orange
    0xFF9B59B6.toInt(), // Purple
    0xFF1ABC9C.toInt(), // Teal
    0xFF95A5A6.toInt(), // Silver
)

private val SwatchSize = 36.dp
private val SwatchBorderWidth = 2.dp

/**
 * A row of color swatches with a "None" clear option and a "Custom…" trigger.
 *
 * @param selectedColor the currently selected ARGB color, or null for no color
 * @param onColorSelected called with the new color (null = clear)
 * @param onCustomClick called when user taps the "Custom…" chip
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorPaletteRow(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // None chip
            NoneColorChip(
                isSelected = selectedColor == null,
                onClick = { onColorSelected(null) }
            )

            // Predefined swatches
            PREDEFINED_TOPIC_COLORS.forEach { colorInt ->
                ColorSwatch(
                    color = Color(colorInt),
                    isSelected = selectedColor == colorInt,
                    onClick = { onColorSelected(colorInt) }
                )
            }

            // Custom chip
            CustomColorChip(
                isCustom = selectedColor != null && selectedColor !in PREDEFINED_TOPIC_COLORS,
                selectedColor = selectedColor?.takeIf { it !in PREDEFINED_TOPIC_COLORS },
                onClick = onCustomClick
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(SwatchSize)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(SwatchBorderWidth, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier.border(SwatchBorderWidth, Color.Transparent, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NoneColorChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier
            .size(SwatchSize)
            .clip(CircleShape)
            .border(SwatchBorderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "No color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CustomColorChip(
    isCustom: Boolean,
    selectedColor: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (isCustom && selectedColor != null) {
        Modifier.background(Color(selectedColor), CircleShape)
    } else {
        Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
    }

    val borderColor = if (isCustom) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(backgroundModifier)
            .border(SwatchBorderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCustom) "Custom" else "Custom…",
            style = MaterialTheme.typography.labelSmall,
            color = if (isCustom && selectedColor != null) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}
