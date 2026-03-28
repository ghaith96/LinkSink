package com.linksink.sync

import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings

sealed interface SchedulingDecision {
    data object EnsurePeriodic : SchedulingDecision
    data object CancelPeriodic : SchedulingDecision
}

object SchedulingPolicy {
    fun decide(syncSettings: SyncSettings): SchedulingDecision {
        val enabled =
            syncSettings.enabled &&
                syncSettings.providerId != SyncProviderId.NONE &&
                syncSettings.isProviderConfigValid

        return if (enabled) SchedulingDecision.EnsurePeriodic else SchedulingDecision.CancelPeriodic
    }
}

