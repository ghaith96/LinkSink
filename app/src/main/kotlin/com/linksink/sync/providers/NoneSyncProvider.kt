package com.linksink.sync.providers

import com.linksink.model.Link

class NoneSyncProvider : SyncProvider {
    override val id: String = SyncProviderId.NONE

    override suspend fun testConnection(config: SyncProviderConfig): Result<Unit> {
        return Result.failure(IllegalStateException("Sync is disabled"))
    }

    override suspend fun sendLink(config: SyncProviderConfig, link: Link): Result<String?> {
        return Result.failure(IllegalStateException("Sync is disabled"))
    }

    override fun close() = Unit
}

