package com.linksink.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME = "link_reminder_notifications"
    }

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    fun scheduleReminders(frequencyHours: Int) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = frequencyHours.toLong(),
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminders() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    fun rescheduleReminders(frequencyHours: Int) {
        cancelReminders()
        scheduleReminders(frequencyHours)
    }
}
