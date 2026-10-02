package com.shivdhikshith.medsafe.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shivdhikshith.medsafe.MedSafeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? MedSafeApplication
                    if (app != null) {
                        // Ensure WorkManager daily expiry check is registered
                        ExpiryNotificationWorker.schedulePeriodicWork(context)

                        // Reschedule active personal reminders
                        val activeReminders = app.container.reminderRepository.getActiveReminders()
                        val scheduler = AndroidReminderScheduler(context)
                        for (reminder in activeReminders) {
                            val med = app.container.medicineRepository.getMedicineWithBatches(reminder.medicineId)
                            val medicineName = med?.medicine?.name ?: ""
                            scheduler.schedule(reminder, medicineName)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore safely
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
