package com.linksink.sync.providers

import com.linksink.model.Link

data class SyncProviderConfig(
    val providerId: String,
    val webhookUrl: String? = null
)

interface SyncProvider {
    val id: String

    suspend fun testConnection(config: SyncProviderConfig): Result<Unit>

    /**
     * Returns an optional provider-specific remote id on success.
     */
    suspend fun sendLink(config: SyncProviderConfig, link: Link): Result<String?>

    /**
     * Optional lifecycle hook for providers that hold resources (e.g. clients).
     * Default is a no-op.
     */
    fun close() = Unit
}

