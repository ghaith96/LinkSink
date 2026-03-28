package com.linksink.viewmodel

import com.linksink.data.AppSettingsStore
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.testing.MainDispatcherRule
import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelProviderTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `default ui state reflects sync provider none disabled`() = runTest {
        val store = FakeAppSettingsStore(
            webhookUrl = null,
            syncSettings = SyncSettings(
                providerId = SyncProviderId.NONE,
                enabled = false,
                isProviderConfigValid = true
            )
        )
        val vm = SettingsViewModel(
            settingsStore = store,
            discordClient = FakeDiscordClient()
        )

        advanceUntilIdle()
        val state = vm.uiState.value

        assertEquals("", state.webhookUrl)
        assertEquals(false, state.isValidUrl)
        assertEquals(SyncProviderId.NONE, state.providerId)
        assertEquals(false, state.syncEnabled)
    }

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `selecting discord provider updates ui state`() = runTest {
        val store = FakeAppSettingsStore(
            webhookUrl = null,
            syncSettings = SyncSettings(
                providerId = SyncProviderId.NONE,
                enabled = false,
                isProviderConfigValid = true
            )
        )
        val vm = SettingsViewModel(
            settingsStore = store,
            discordClient = FakeDiscordClient()
        )

        vm.setProvider(SyncProviderId.DISCORD_WEBHOOK)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SyncProviderId.DISCORD_WEBHOOK, state.providerId)
    }
}

private class FakeDiscordClient : DiscordWebhookClient() {
    override suspend fun testWebhook(webhookUrl: String): Result<Unit> = Result.success(Unit)
}

private class FakeAppSettingsStore(
    webhookUrl: String?,
    syncSettings: SyncSettings
) : AppSettingsStore {
    private val webhookUrlFlow = MutableStateFlow(webhookUrl)
    private val syncSettingsFlow = MutableStateFlow(syncSettings)

    override val webhookUrl: Flow<String?> = webhookUrlFlow
    override val syncSettings: Flow<SyncSettings> = syncSettingsFlow

    override val syncProviderId: Flow<String?> = flowOf(syncSettings.providerId)
    override val syncEnabledRaw: Flow<Boolean?> = flowOf(syncSettings.enabled)

    override suspend fun setWebhookUrl(url: String) {
        webhookUrlFlow.value = url
    }

    override suspend fun clearWebhookUrl() {
        webhookUrlFlow.value = null
    }

    override suspend fun setOnboardingComplete(complete: Boolean) = Unit

    override suspend fun setSyncProviderId(providerId: String) {
        syncSettingsFlow.value = syncSettingsFlow.value.copy(providerId = providerId)
    }

    override suspend fun clearSyncProviderId() {
        syncSettingsFlow.value = syncSettingsFlow.value.copy(providerId = SyncProviderId.NONE)
    }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        syncSettingsFlow.value = syncSettingsFlow.value.copy(enabled = enabled)
    }

    override suspend fun clearSyncEnabled() {
        syncSettingsFlow.value = syncSettingsFlow.value.copy(enabled = false)
    }
}

