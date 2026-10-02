package com.shivdhikshith.medsafe.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shivdhikshith.medsafe.MedSafeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_REMINDER) return

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Medicine Reminder"
        var medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: ""
        val recurrence = intent.getStringExtra(EXTRA_RECURRENCE) ?: "DAILY"

        if (reminderId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? MedSafeApplication
                if (app != null) {
                    val reminder = app.container.reminderRepository.getReminderById(reminderId)
                    if (reminder != null && reminder.isEnabled) {
                        if (medicineName.isBlank() && medicineId != -1L) {
                            val med = app.container.medicineRepository.getMedicineWithBatches(medicineId)
                            medicineName = med?.medicine?.name ?: ""
                        }

                        val notificationHelper = NotificationHelper(context)
                        notificationHelper.showPersonalReminder(
                            notificationId = reminderId.toInt(),
                            reminderTitle = title,
                            medicineName = medicineName
                        )

                        when {
                            recurrence.equals("DAILY", ignoreCase = true) ||
                            recurrence.equals("WEEKDAYS", ignoreCase = true) -> {
                                val scheduler = AndroidReminderScheduler(context)
                                scheduler.schedule(reminder, medicineName)
                            }
                            else -> {
                                app.container.reminderRepository.updateReminderEnabled(reminderId, false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore safely
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.shivdhikshith.medsafe.ACTION_TRIGGER_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICINE_ID = "extra_medicine_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_RECURRENCE = "extra_recurrence"
    }
}
