package com.linksink.sync.settings

import com.linksink.data.SettingsStore
import com.linksink.sync.providers.SyncProviderId

object SyncSettingsResolver {
    fun resolve(
        storedProviderId: String?,
        storedSyncEnabled: Boolean?,
        storedWebhookUrl: String?
    ): SyncSettings {
        val isWebhookValid = storedWebhookUrl?.let { SettingsStore.isValidWebhookUrl(it) } == true

        val migratedFromLegacyWebhook =
            storedProviderId == null && storedSyncEnabled == null && isWebhookValid

        val providerId = when {
            migratedFromLegacyWebhook -> SyncProviderId.DISCORD_WEBHOOK
            storedProviderId == null -> SyncProviderId.NONE
            storedProviderId == SyncProviderId.NONE -> SyncProviderId.NONE
            storedProviderId == SyncProviderId.DISCORD_WEBHOOK -> SyncProviderId.DISCORD_WEBHOOK
            else -> SyncProviderId.NONE
        }

        val enabled = when {
            providerId == SyncProviderId.NONE -> false
            migratedFromLegacyWebhook -> true
            storedSyncEnabled != null -> storedSyncEnabled
            else -> false
        }

        return SyncSettings(
            providerId = providerId,
            enabled = enabled,
            isProviderConfigValid = when (providerId) {
                SyncProviderId.NONE -> true
                SyncProviderId.DISCORD_WEBHOOK -> isWebhookValid
                else -> true
            }
        )
    }
}

