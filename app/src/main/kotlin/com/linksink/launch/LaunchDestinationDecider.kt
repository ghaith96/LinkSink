package com.linksink.launch

sealed interface LaunchDestination {
    data object Main : LaunchDestination
    data object Settings : LaunchDestination
}

class LaunchDestinationDecider {
    fun decide(
        isOnboardingComplete: Boolean,
        hasWebhookUrl: Boolean
    ): LaunchDestination {
        return LaunchDestination.Main
    }
}

