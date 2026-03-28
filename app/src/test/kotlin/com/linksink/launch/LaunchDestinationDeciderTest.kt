package com.linksink.launch

import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchDestinationDeciderTest {

    @Test
    fun `default is main even when onboarding incomplete and no webhook`() {
        val decider = LaunchDestinationDecider()

        val destination = decider.decide(
            isOnboardingComplete = false,
            hasWebhookUrl = false
        )

        assertEquals(LaunchDestination.Main, destination)
    }
}

