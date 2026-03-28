package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.AppSettingsStore
import com.linksink.data.SettingsStore
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.sync.providers.SyncProviderId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val providerId: String = SyncProviderId.NONE,
    val syncEnabled: Boolean = false,
    val webhookUrl: String = "",
    val isValidUrl: Boolean = false,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val testResult: TestResult? = null,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false
)

sealed interface TestResult {
    data object Success : TestResult
    data class Failure(val message: String) : TestResult
}

class SettingsViewModel(
    private val settingsStore: AppSettingsStore,
    private val discordClient: DiscordWebhookClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsStore.webhookUrl,
                settingsStore.syncSettings
            ) { url, sync ->
                url to sync
            }.collect { (url, sync) ->
                val currentUrl = url ?: ""
                _uiState.update {
                    it.copy(
                        providerId = sync.providerId,
                        syncEnabled = sync.enabled,
                        webhookUrl = currentUrl,
                        isValidUrl = SettingsStore.isValidWebhookUrl(currentUrl),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onWebhookUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                webhookUrl = url,
                isValidUrl = SettingsStore.isValidWebhookUrl(url),
                testResult = null,
                saveSuccess = false
            )
        }
    }

    fun testConnection() {
        val url = _uiState.value.webhookUrl.trim()
        if (!SettingsStore.isValidWebhookUrl(url)) return

        _uiState.update { it.copy(isTesting = true, testResult = null) }

        viewModelScope.launch {
            val result = discordClient.testWebhook(url)
            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = if (result.isSuccess) {
                        TestResult.Success
                    } else {
                        TestResult.Failure(
                            result.exceptionOrNull()?.message ?: "Connection failed"
                        )
                    }
                )
            }
        }
    }

    fun saveSettings() {
        val url = _uiState.value.webhookUrl.trim()
        if (!SettingsStore.isValidWebhookUrl(url)) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            settingsStore.setWebhookUrl(url)
            settingsStore.setSyncProviderId(SyncProviderId.DISCORD_WEBHOOK)
            settingsStore.setSyncEnabled(true)
            settingsStore.setOnboardingComplete(true)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            }
        }
    }

    fun clearSettings() {
        viewModelScope.launch {
            settingsStore.clearWebhookUrl()
            settingsStore.setSyncEnabled(false)
            settingsStore.setSyncProviderId(SyncProviderId.NONE)
            _uiState.update {
                it.copy(
                    webhookUrl = "",
                    isValidUrl = false,
                    testResult = null,
                    saveSuccess = false
                )
            }
        }
    }

    fun setProvider(providerId: String) {
        viewModelScope.launch {
            settingsStore.setSyncProviderId(providerId)
            if (providerId == SyncProviderId.NONE) {
                settingsStore.setSyncEnabled(false)
            }
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setSyncEnabled(enabled)
        }
    }

    fun dismissTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }
}
