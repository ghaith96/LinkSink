package com.linksink.sync

import com.linksink.sync.providers.SyncProviderId
import com.linksink.sync.settings.SyncSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SchedulingPolicyTest {

    @Test
    fun `disabled sync results in cancel periodic and no trigger`() {
        val policy = SchedulingPolicy.decide(
            syncSettings = SyncSettings(
                providerId = SyncProviderId.NONE,
                enabled = false,
                isProviderConfigValid = true
            )
        )

        assertEquals(SchedulingDecision.CancelPeriodic, policy)
    }

    @Test
    fun `enabled discord results in schedule periodic`() {
        val policy = SchedulingPolicy.decide(
            syncSettings = SyncSettings(
                providerId = SyncProviderId.DISCORD_WEBHOOK,
                enabled = true,
                isProviderConfigValid = true
            )
        )

        assertEquals(SchedulingDecision.EnsurePeriodic, policy)
    }
}

