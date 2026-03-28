package com.linksink.sync.settings

data class SyncSettings(
    val providerId: String,
    val enabled: Boolean,
    val isProviderConfigValid: Boolean
)

