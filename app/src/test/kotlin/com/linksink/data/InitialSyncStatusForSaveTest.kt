package com.linksink.data

import com.linksink.model.SyncStatus
import com.linksink.model.WebhookResolution
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class InitialSyncStatusForSaveTest {

    @Test
    fun `local-only topic resolution forces LOCAL_ONLY even when sync enabled`() {
        val syncSettings = SyncSettings(
            providerId = SyncProviderId.DISCORD_WEBHOOK,
            enabled = true,
            isProviderConfigValid = true
        )

        val status = initialSyncStatusForSave(
            syncSettings = syncSettings,
            resolution = WebhookResolution.LocalOnly
        )

        assertEquals(SyncStatus.LOCAL_ONLY, status)
    }
}

