package com.linksink.notifications

import org.junit.Test

class NotificationHelperTest {

    @Test
    fun `NotificationHelper class exists`() {
        val helperClass = NotificationHelper::class.java
        assert(helperClass != null) { "NotificationHelper class should exist" }
    }

    @Test
    fun `NotificationHelper has createNotificationChannel method`() {
        val helperClass = NotificationHelper::class.java
        val method = helperClass.methods.find { it.name == "createNotificationChannel" }
        assert(method != null) { "createNotificationChannel method should exist" }
    }

    @Test
    fun `NotificationHelper has buildLinkReminderNotification method`() {
        val helperClass = NotificationHelper::class.java
        val method = helperClass.methods.find { it.name == "buildLinkReminderNotification" }
        assert(method != null) { "buildLinkReminderNotification method should exist" }
    }

    @Test
    fun `NotificationHelper has showNotification method`() {
        val helperClass = NotificationHelper::class.java
        val method = helperClass.methods.find { it.name == "showNotification" }
        assert(method != null) { "showNotification method should exist" }
    }
}
