package com.shivdhikshith.medsafe.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.shivdhikshith.medsafe.domain.model.Reminder
import java.util.Calendar

interface ReminderScheduler {
    fun schedule(reminder: Reminder, medicineName: String = "")
    fun cancel(reminderId: Long)
}

class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(reminder: Reminder, medicineName: String) {
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }

        val triggerTime = computeNextTriggerTime(reminder.timeInMillis, reminder.recurrence)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_TRIGGER_REMINDER
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_MEDICINE_ID, reminder.medicineId)
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderBroadcastReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(ReminderBroadcastReceiver.EXTRA_RECURRENCE, reminder.recurrence)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled reminder ${reminder.id} for $triggerTime")
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Failed to schedule exact alarm: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e("ReminderScheduler", "Fallback alarm scheduling failed: ${fallbackEx.message}")
            }
        }
    }

    override fun cancel(reminderId: Long) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        fun computeNextTriggerTime(scheduledTimeMillis: Long, recurrence: String): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                timeInMillis = scheduledTimeMillis
            }

            val hour = target.get(Calendar.HOUR_OF_DAY)
            val minute = target.get(Calendar.MINUTE)

            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (next.timeInMillis <= now.timeInMillis) {
                next.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (recurrence.equals("WEEKDAYS", ignoreCase = true)) {
                while (next.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    next.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    next.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            return next.timeInMillis
        }
    }
}
