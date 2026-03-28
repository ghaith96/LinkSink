package com.linksink.sync.providers

class SyncProviderRegistry(
    private val providers: List<SyncProvider>
) {
    fun availableProviderIds(): Set<String> = providers.map { it.id }.toSet()

    fun resolve(providerId: String): SyncProvider? =
        providers.firstOrNull { it.id == providerId }
}

