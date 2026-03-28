package com.linksink.data

import com.linksink.sync.settings.SyncSettings
import kotlinx.coroutines.flow.Flow

/**
 * Narrow read-only settings surface for sync-related decisions.
 * This exists so sync behavior can be unit-tested without Android DataStore.
 */
interface SyncSettingsStore {
    val webhookUrl: Flow<String?>
    val syncSettings: Flow<SyncSettings>
}

