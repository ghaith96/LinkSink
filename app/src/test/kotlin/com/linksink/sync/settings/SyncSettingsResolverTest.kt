package com.linksink.sync.settings

import com.linksink.sync.providers.SyncProviderId
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSettingsResolverTest {

    @Test
    fun `default resolves to none disabled`() {
        val settings = SyncSettingsResolver.resolve(
            storedProviderId = null,
            storedSyncEnabled = null,
            storedWebhookUrl = null
        )

        assertEquals(SyncProviderId.NONE, settings.providerId)
        assertEquals(false, settings.enabled)
        assertEquals(true, settings.isProviderConfigValid)
    }

    @Test
    fun `upgrade path - webhook present with no provider keys resolves to discord enabled`() {
        val settings = SyncSettingsResolver.resolve(
            storedProviderId = null,
            storedSyncEnabled = null,
            storedWebhookUrl = "https://discord.com/api/webhooks/1/abc"
        )

        assertEquals(SyncProviderId.DISCORD_WEBHOOK, settings.providerId)
        assertEquals(true, settings.enabled)
        assertEquals(true, settings.isProviderConfigValid)
    }

    @Test
    fun `explicit stored provider and enabled are respected`() {
        val settings = SyncSettingsResolver.resolve(
            storedProviderId = SyncProviderId.DISCORD_WEBHOOK,
            storedSyncEnabled = false,
            storedWebhookUrl = "https://discord.com/api/webhooks/1/abc"
        )

        assertEquals(SyncProviderId.DISCORD_WEBHOOK, settings.providerId)
        assertEquals(false, settings.enabled)
        assertEquals(true, settings.isProviderConfigValid)
    }

    @Test
    fun `unknown provider id falls back to none disabled`() {
        val settings = SyncSettingsResolver.resolve(
            storedProviderId = "something_else",
            storedSyncEnabled = true,
            storedWebhookUrl = "https://discord.com/api/webhooks/1/abc"
        )

        assertEquals(SyncProviderId.NONE, settings.providerId)
        assertEquals(false, settings.enabled)
        assertEquals(true, settings.isProviderConfigValid)
    }

    @Test
    fun `discord provider with invalid webhook config is marked invalid`() {
        val settings = SyncSettingsResolver.resolve(
            storedProviderId = SyncProviderId.DISCORD_WEBHOOK,
            storedSyncEnabled = true,
            storedWebhookUrl = "https://example.com/not-a-discord-webhook"
        )

        assertEquals(SyncProviderId.DISCORD_WEBHOOK, settings.providerId)
        assertEquals(true, settings.enabled)
        assertEquals(false, settings.isProviderConfigValid)
    }
}

