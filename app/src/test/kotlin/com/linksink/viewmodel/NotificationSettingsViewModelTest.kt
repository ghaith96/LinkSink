package com.linksink.viewmodel

import org.junit.Test

class NotificationSettingsViewModelTest {

    @Test
    fun `NotificationSettingsViewModel class exists`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        assert(viewModelClass != null) { "NotificationSettingsViewModel class should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has reminderEnabled property`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val field = viewModelClass.declaredFields.find { it.name.contains("reminderEnabled") }
        assert(field != null) { "reminderEnabled property should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has reminderFrequencyHours property`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val field = viewModelClass.declaredFields.find { it.name.contains("reminderFrequencyHours") }
        assert(field != null) { "reminderFrequencyHours property should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has reminderMaxDaily property`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val field = viewModelClass.declaredFields.find { it.name.contains("reminderMaxDaily") }
        assert(field != null) { "reminderMaxDaily property should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has setReminderEnabled method`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val method = viewModelClass.methods.find { it.name == "setReminderEnabled" }
        assert(method != null) { "setReminderEnabled method should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has setReminderFrequency method`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val method = viewModelClass.methods.find { it.name == "setReminderFrequency" }
        assert(method != null) { "setReminderFrequency method should exist" }
    }

    @Test
    fun `NotificationSettingsViewModel has setReminderMaxDaily method`() {
        val viewModelClass = NotificationSettingsViewModel::class.java
        val method = viewModelClass.methods.find { it.name == "setReminderMaxDaily" }
        assert(method != null) { "setReminderMaxDaily method should exist" }
    }
}
