package com.linksink.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBack: () -> Unit
) {
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val frequencyHours by viewModel.reminderFrequencyHours.collectAsState()
    val maxDaily by viewModel.reminderMaxDaily.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        "Link Reminders",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Reminders")
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { viewModel.setReminderEnabled(it) }
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Frequency: Every $frequencyHours hour${if (frequencyHours != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = frequencyHours.toFloat(),
                            onValueChange = { viewModel.setReminderFrequency(it.toInt()) },
                            valueRange = 1f..72f,
                            steps = 70,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Maximum per day: $maxDaily notification${if (maxDaily != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = maxDaily.toFloat(),
                            onValueChange = { viewModel.setReminderMaxDaily(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (reminderEnabled) {
                Text(
                    "You'll receive notifications about random unread links based on your settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
