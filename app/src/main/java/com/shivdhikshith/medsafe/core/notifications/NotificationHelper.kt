package com.shivdhikshith.medsafe.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shivdhikshith.medsafe.MainActivity
import com.shivdhikshith.medsafe.R
import com.shivdhikshith.medsafe.core.permissions.PermissionUtils

open class NotificationHelper(private val context: Context? = null) {

    private val notificationManager by lazy {
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    /**
     * Shows an organizational expiry alert for a medicine batch.
     * Complies with MEDSAFE non-medical advisory standards:
     * - Reminders: "Medicine Expiry Reminder", "$medicineName batch $batchNumber expires in X days."
     * - Expired: "Medicine Expired", "$medicineName batch $batchNumber expired."
     *
     * Returns true if notification was posted to NotificationManager, false otherwise.
     */
    open fun showExpiryAlert(
        notificationId: Int,
        medicineName: String,
        batchNumber: String,
        daysRemaining: Long
    ): Boolean {
        val currentContext = context ?: run {
            Log.w(TAG, "showExpiryAlert FAILED: context is null")
            return false
        }
        if (!PermissionUtils.hasNotificationPermission(currentContext)) {
            Log.w(TAG, "showExpiryAlert FAILED: POST_NOTIFICATIONS permission is not granted")
            return false
        }

        NotificationChannels.createNotificationChannels(currentContext)

        val mgrCompat = androidx.core.app.NotificationManagerCompat.from(currentContext)
        if (!mgrCompat.areNotificationsEnabled()) {
            Log.w(TAG, "showExpiryAlert FAILED: Notifications are disabled at app-level in system settings")
            return false
        }

        val mgr = notificationManager ?: (currentContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
        if (mgr == null) {
            Log.e(TAG, "showExpiryAlert FAILED: NotificationManager is null, cannot notify")
            return false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = mgr.getNotificationChannel(NotificationChannels.CHANNEL_EXPIRY_ALERTS)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                Log.w(TAG, "showExpiryAlert FAILED: Channel ${NotificationChannels.CHANNEL_EXPIRY_ALERTS} is disabled/blocked in system settings")
                return false
            }
        }

        val displayBatch = if (batchNumber.isNotBlank()) batchNumber.trim() else "Standard"
        val (title, message) = when {
            daysRemaining < 0 -> Pair(
                "Medicine Expired",
                "$medicineName batch $displayBatch expired."
            )
            daysRemaining == 0L -> Pair(
                "Medicine Expiry Reminder",
                "$medicineName batch $displayBatch reaches expiry date today."
            )
            daysRemaining == 1L -> Pair(
                "Medicine Expiry Reminder",
                "$medicineName batch $displayBatch expires in 1 day."
            )
            else -> Pair(
                "Medicine Expiry Reminder",
                "$medicineName batch $displayBatch expires in $daysRemaining days."
            )
        }

        val intent = Intent(currentContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            currentContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(currentContext, NotificationChannels.CHANNEL_EXPIRY_ALERTS)
            .setSmallIcon(R.drawable.ic_medsafe_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            mgrCompat.notify(notificationId, notification)
            Log.i(TAG, "showExpiryAlert SUCCESS: Posted notificationId=$notificationId to channel ${NotificationChannels.CHANNEL_EXPIRY_ALERTS}, title='$title', message='$message'")
            true
        } catch (e: Exception) {
            Log.e(TAG, "showExpiryAlert FAILED: Error posting expiry notification $notificationId: ${e.message}", e)
            false
        }
    }

    /**
     * Shows a personal user-created medication reminder notification.
     */
    open fun showPersonalReminder(
        notificationId: Int,
        reminderTitle: String,
        medicineName: String
    ): Boolean {
        val currentContext = context ?: run {
            Log.w(TAG, "showPersonalReminder FAILED: context is null")
            return false
        }
        if (!PermissionUtils.hasNotificationPermission(currentContext)) {
            Log.w(TAG, "showPersonalReminder FAILED: POST_NOTIFICATIONS permission is not granted")
            return false
        }

        NotificationChannels.createNotificationChannels(currentContext)

        val mgrCompat = androidx.core.app.NotificationManagerCompat.from(currentContext)
        if (!mgrCompat.areNotificationsEnabled()) {
            Log.w(TAG, "showPersonalReminder FAILED: Notifications are disabled at app-level in system settings")
            return false
        }

        val mgr = notificationManager ?: (currentContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
        if (mgr == null) {
            Log.e(TAG, "showPersonalReminder FAILED: NotificationManager is null, cannot notify")
            return false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = mgr.getNotificationChannel(NotificationChannels.CHANNEL_REMINDERS)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                Log.w(TAG, "showPersonalReminder FAILED: Channel ${NotificationChannels.CHANNEL_REMINDERS} is disabled/blocked in system settings")
                return false
            }
        }

        val title = reminderTitle.ifBlank { "Personal Medicine Reminder" }
        val message = if (medicineName.isNotBlank()) {
            "Scheduled reminder for $medicineName."
        } else {
            "Personal schedule reminder."
        }

        val intent = Intent(currentContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            currentContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(currentContext, NotificationChannels.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_medsafe_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            mgrCompat.notify(notificationId, notification)
            Log.i(TAG, "showPersonalReminder SUCCESS: Posted notificationId=$notificationId to channel ${NotificationChannels.CHANNEL_REMINDERS}, title='$title', message='$message'")
            true
        } catch (e: Exception) {
            Log.e(TAG, "showPersonalReminder FAILED: Error posting reminder $notificationId: ${e.message}", e)
            false
        }
    }

    open fun cancelNotification(notificationId: Int) {
        notificationManager?.cancel(notificationId)
    }

    companion object {
        private const val TAG = "NotificationHelper"
    }
}
