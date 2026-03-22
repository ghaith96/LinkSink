package com.linksink.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.linksink.LinkSinkApp
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncWorker starting")

        val app = applicationContext as? LinkSinkApp
        if (app == null) {
            Log.e(TAG, "Could not get LinkSinkApp")
            return Result.failure()
        }

        return try {
            val result = app.repository.syncPendingLinks()
            if (result.isSuccess) {
                val synced = result.getOrDefault(0)
                Log.d(TAG, "SyncWorker completed: $synced links synced")
                Result.success()
            } else {
                Log.e(TAG, "SyncWorker failed: ${result.exceptionOrNull()?.message}")
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker exception", e)
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRIES = 3
        private const val UNIQUE_WORK_NAME = "linksink_sync"
        private const val PERIODIC_WORK_NAME = "linksink_periodic_sync"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun enqueueOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueue(request)

            Log.d(TAG, "One-time sync enqueued")
        }

        fun enqueuePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )

            Log.d(TAG, "Periodic sync enqueued")
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(PERIODIC_WORK_NAME)

            Log.d(TAG, "Periodic sync cancelled")
        }
    }
}
