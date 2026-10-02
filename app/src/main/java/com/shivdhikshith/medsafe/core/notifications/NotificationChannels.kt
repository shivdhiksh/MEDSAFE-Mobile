package com.shivdhikshith.medsafe.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationChannels {
    const val CHANNEL_EXPIRY_ALERTS = "medsafe_expiry_alerts_v2"
    const val CHANNEL_REMINDERS = "medsafe_personal_reminders"
    private const val TAG = "NotificationChannels"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Clean up obsolete/deleted legacy channel so it does not linger in system settings
            try {
                val legacyChannel = notificationManager.getNotificationChannel("medsafe_expiry_alerts")
                if (legacyChannel != null) {
                    notificationManager.deleteNotificationChannel("medsafe_expiry_alerts")
                    Log.d(TAG, "Deleted legacy notification channel: medsafe_expiry_alerts")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning legacy channel: ${e.message}")
            }

            val expiryChannel = NotificationChannel(
                CHANNEL_EXPIRY_ALERTS,
                "Medicine Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when medication batches approach expiry dates"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Personal Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "User configured medicine schedule reminders"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(expiryChannel)
            notificationManager.createNotificationChannel(remindersChannel)
            Log.d(TAG, "Notification channels registered successfully (v2)")
        }
    }
}
