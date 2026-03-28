package com.linksink.data

import com.linksink.sync.settings.SyncSettings
import kotlinx.coroutines.flow.Flow

/**
 * Settings access required by viewmodels and sync coordination.
 * Implemented by [SettingsStore] (DataStore-backed), but abstracted for unit tests.
 */
interface AppSettingsStore : SyncSettingsStore {
    val syncProviderId: Flow<String?>
    val syncEnabledRaw: Flow<Boolean?>

    suspend fun setWebhookUrl(url: String)
    suspend fun clearWebhookUrl()

    suspend fun setSyncProviderId(providerId: String)
    suspend fun clearSyncProviderId()
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun clearSyncEnabled()

    suspend fun setOnboardingComplete(complete: Boolean)
}

