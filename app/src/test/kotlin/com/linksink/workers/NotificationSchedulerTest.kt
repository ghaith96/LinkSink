package com.linksink.workers

import org.junit.Test

class NotificationSchedulerTest {

    @Test
    fun `NotificationScheduler class exists`() {
        val schedulerClass = NotificationScheduler::class.java
        assert(schedulerClass != null) { "NotificationScheduler class should exist" }
    }

    @Test
    fun `NotificationScheduler has scheduleReminders method`() {
        val schedulerClass = NotificationScheduler::class.java
        val method = schedulerClass.methods.find { it.name == "scheduleReminders" }
        assert(method != null) { "scheduleReminders method should exist" }
    }

    @Test
    fun `NotificationScheduler has cancelReminders method`() {
        val schedulerClass = NotificationScheduler::class.java
        val method = schedulerClass.methods.find { it.name == "cancelReminders" }
        assert(method != null) { "cancelReminders method should exist" }
    }

    @Test
    fun `NotificationScheduler has rescheduleReminders method`() {
        val schedulerClass = NotificationScheduler::class.java
        val method = schedulerClass.methods.find { it.name == "rescheduleReminders" }
        assert(method != null) { "rescheduleReminders method should exist" }
    }
}
