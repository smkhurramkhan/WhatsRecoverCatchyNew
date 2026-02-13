package com.catchyapps.whatsdelete.appnotifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that ensures the notification listener service
 * stays alive. Runs every 15 minutes and restarts the service if it's not running.
 */
class ServiceRestartWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ServiceRestartWorker"
        private const val UNIQUE_WORK_NAME = "service_keep_alive"

        /**
         * Schedule the periodic keep-alive worker.
         * Uses KEEP policy so it won't reset the timer if already scheduled.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceRestartWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.tag(TAG).d("Periodic service keep-alive scheduled (every 15 min)")
        }

        /**
         * Cancel the periodic keep-alive worker.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Timber.tag(TAG).d("Periodic service keep-alive cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Keep-alive worker triggered, checking service status...")

        return try {
            if (!isNotificationListenerEnabled()) {
                Timber.tag(TAG).w("Notification listener permission not granted, skipping restart")
                return Result.success()
            }

            restartServiceIfNeeded()
            Result.success()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in keep-alive worker")
            Result.retry()
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            applicationContext.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val componentName = ComponentName(
            applicationContext,
            AppDeletedMessagesNotificationService::class.java
        ).flattenToString()

        return flat.contains(componentName)
    }

    private fun restartServiceIfNeeded() {
        val intent = Intent(applicationContext, AppDeletedMessagesNotificationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(applicationContext, intent)
            } else {
                applicationContext.startService(intent)
            }
            Timber.tag(TAG).d("Service restart command sent successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to restart service")
        }
    }
}
