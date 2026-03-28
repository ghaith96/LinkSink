package com.linksink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linksink.ui.theme.Spacing

/**
 * Dialog for entering a custom hex color code (e.g. "#5865F2").
 *
 * @param initialColor the currently selected color to pre-fill, or null
 * @param onColorConfirmed called with the parsed ARGB Int when user confirms
 * @param onDismiss called when dialog is dismissed without selecting
 */
@Composable
internal fun ColorPickerDialog(
    initialColor: Int?,
    onColorConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHex = initialColor?.let { colorIntToHex(it) } ?: ""
    var hexInput by remember { mutableStateOf(initialHex) }
    val parsedColor = parseHexColor(hexInput)
    val isValid = parsedColor != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Color") },
        text = {
            Column {
                Text(
                    text = "Enter a hex color code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            // Keep "#" prefix and limit to 7 chars
                            val cleaned = input.trimStart().let {
                                if (!it.startsWith("#")) "#$it" else it
                            }.take(7)
                            hexInput = cleaned
                        },
                        label = { Text("Hex code") },
                        placeholder = { Text("#5865F2") },
                        isError = hexInput.length > 1 && !isValid,
                        supportingText = if (hexInput.length > 1 && !isValid) {
                            { Text("Enter a valid 6-digit hex color") }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isValid) Color(parsedColor!!) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    parsedColor?.let { onColorConfirmed(it) }
                },
                enabled = isValid
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(Spacing.lg)
    )
}

private fun parseHexColor(hex: String): Int? {
    val cleaned = hex.trimStart('#')
    if (cleaned.length != 6) return null
    return try {
        val rgb = cleaned.toLong(16)
        (0xFF000000L or rgb).toInt()
    } catch (e: NumberFormatException) {
        null
    }
}

private fun colorIntToHex(colorInt: Int): String {
    val rgb = colorInt and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}
