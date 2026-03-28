package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linksink.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {

    val reminderEnabled: StateFlow<Boolean> = settingsStore.reminderEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val reminderFrequencyHours: StateFlow<Int> = settingsStore.reminderFrequencyHours.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 24
    )

    val reminderMaxDaily: StateFlow<Int> = settingsStore.reminderMaxDaily.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setReminderEnabled(enabled)
        }
    }

    fun setReminderFrequency(hours: Int) {
        viewModelScope.launch {
            settingsStore.setReminderFrequencyHours(hours)
        }
    }

    fun setReminderMaxDaily(count: Int) {
        viewModelScope.launch {
            settingsStore.setReminderMaxDaily(count)
        }
    }
}

class NotificationSettingsViewModelFactory(
    private val settingsStore: SettingsStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationSettingsViewModel::class.java)) {
            return NotificationSettingsViewModel(settingsStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
