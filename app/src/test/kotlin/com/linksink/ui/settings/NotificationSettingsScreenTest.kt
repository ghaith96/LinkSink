package com.linksink.ui.settings

import org.junit.Test

class NotificationSettingsScreenTest {

    @Test
    fun `NotificationSettingsScreen function exists`() {
        val screenClass = Class.forName("com.linksink.ui.settings.NotificationSettingsScreenKt")
        val method = screenClass.methods.find { it.name == "NotificationSettingsScreen" }
        assert(method != null) { "NotificationSettingsScreen composable should exist" }
    }
}
