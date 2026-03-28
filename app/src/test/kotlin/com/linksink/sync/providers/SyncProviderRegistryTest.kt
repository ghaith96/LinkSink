package com.linksink.sync.providers

import com.linksink.model.Link
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncProviderRegistryTest {

    @Test
    fun `registry exposes none and discord providers`() {
        val registry = SyncProviderRegistry(
            providers = listOf(
                NoneSyncProvider(),
                FakeSyncProvider(id = SyncProviderId.DISCORD_WEBHOOK)
            )
        )

        assertTrue(registry.availableProviderIds().contains(SyncProviderId.NONE))
        assertTrue(registry.availableProviderIds().contains(SyncProviderId.DISCORD_WEBHOOK))
    }

    @Test
    fun `resolve returns provider by id`() {
        val discord = FakeSyncProvider(id = SyncProviderId.DISCORD_WEBHOOK)
        val registry = SyncProviderRegistry(
            providers = listOf(
                NoneSyncProvider(),
                discord
            )
        )

        val resolved = registry.resolve(SyncProviderId.DISCORD_WEBHOOK)

        assertNotNull(resolved)
        assertTrue(resolved === discord)
    }
}

private class FakeSyncProvider(
    override val id: String
) : SyncProvider {
    override suspend fun testConnection(config: SyncProviderConfig): Result<Unit> =
        Result.success(Unit)

    override suspend fun sendLink(config: SyncProviderConfig, link: Link): Result<String?> =
        Result.success(null)

    override fun close() = Unit
}

