package com.shivdhikshith.medsafe.core.notifications

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shivdhikshith.medsafe.MedSafeApplication
import java.util.concurrent.TimeUnit

class ExpiryNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "ExpiryNotificationWorker.doWork() execution started")
        return try {
            val app = applicationContext as? MedSafeApplication ?: run {
                Log.e(TAG, "applicationContext is not MedSafeApplication, failing work")
                return Result.failure()
            }
            val result = app.container.checkAndNotifyExpiryUseCase()
            Log.d(TAG, "ExpiryNotificationWorker finished with: $result")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ExpiryNotificationWorker execution failed: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "com.shivdhikshith.medsafe.EXPIRY_NOTIFICATION_WORK"
        const val IMMEDIATE_WORK_NAME = "com.shivdhikshith.medsafe.IMMEDIATE_EXPIRY_WORK"
        private const val TAG = "ExpiryWorker"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
                24, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Periodic 24h work scheduled")
        }

        fun enqueueImmediateWork(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ExpiryNotificationWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Immediate one-time work enqueued")
        }
    }
}
