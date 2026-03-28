package com.linksink.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.linksink.LinkSinkApp
import com.linksink.data.SettingsStore
import com.linksink.notifications.NotificationHelper
import kotlinx.coroutines.flow.first

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsStore = SettingsStore(applicationContext)
        
        val reminderEnabled = settingsStore.reminderEnabled.first()
        if (!reminderEnabled) {
            return Result.success()
        }

        settingsStore.resetNotificationCountIfNeeded()

        val app = applicationContext as? LinkSinkApp
        if (app == null) {
            return Result.failure()
        }

        val randomLink = app.repository.getRandomUnreadLink()
        
        if (randomLink != null) {
            val allowed = settingsStore.tryIncrementNotificationCount()
            if (allowed) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.createNotificationChannel()
                
                val notification = notificationHelper.buildLinkReminderNotification(randomLink)
                notificationHelper.showNotification(
                    NotificationHelper.NOTIFICATION_ID_LINK_REMINDER,
                    notification
                )
            }
        }
        
        return Result.success()
    }
}

