package com.linksink.data

import org.junit.Test

class SettingsStoreTest {

    @Test
    fun `SettingsStore has reminderEnabled property`() {
        val storeClass = SettingsStore::class.java
        val field = storeClass.declaredFields.find { it.name.contains("reminderEnabled") }
        assert(field != null) { "reminderEnabled property should exist" }
    }

    @Test
    fun `SettingsStore has reminderFrequencyHours property`() {
        val storeClass = SettingsStore::class.java
        val field = storeClass.declaredFields.find { it.name.contains("reminderFrequencyHours") }
        assert(field != null) { "reminderFrequencyHours property should exist" }
    }

    @Test
    fun `SettingsStore has reminderMaxDaily property`() {
        val storeClass = SettingsStore::class.java
        val field = storeClass.declaredFields.find { it.name.contains("reminderMaxDaily") }
        assert(field != null) { "reminderMaxDaily property should exist" }
    }

    @Test
    fun `SettingsStore has setReminderEnabled method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "setReminderEnabled" }
        assert(method != null) { "setReminderEnabled method should exist" }
    }

    @Test
    fun `SettingsStore has setReminderFrequencyHours method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "setReminderFrequencyHours" }
        assert(method != null) { "setReminderFrequencyHours method should exist" }
    }

    @Test
    fun `SettingsStore has setReminderMaxDaily method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "setReminderMaxDaily" }
        assert(method != null) { "setReminderMaxDaily method should exist" }
    }

    @Test
    fun `SettingsStore has lastNotificationDate property`() {
        val storeClass = SettingsStore::class.java
        val field = storeClass.declaredFields.find { it.name.contains("lastNotificationDate") }
        assert(field != null) { "lastNotificationDate property should exist" }
    }

    @Test
    fun `SettingsStore has todayNotificationCount property`() {
        val storeClass = SettingsStore::class.java
        val field = storeClass.declaredFields.find { it.name.contains("todayNotificationCount") }
        assert(field != null) { "todayNotificationCount property should exist" }
    }

    @Test
    fun `SettingsStore has incrementNotificationCount method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "incrementNotificationCount" }
        assert(method != null) { "incrementNotificationCount method should exist" }
    }

    @Test
    fun `SettingsStore has tryIncrementNotificationCount method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "tryIncrementNotificationCount" }
        assert(method != null) { "tryIncrementNotificationCount method should exist" }
    }

    @Test
    fun `SettingsStore has resetNotificationCountIfNeeded method`() {
        val storeClass = SettingsStore::class.java
        val method = storeClass.methods.find { it.name == "resetNotificationCountIfNeeded" }
        assert(method != null) { "resetNotificationCountIfNeeded method should exist" }
    }
}
