package com.shivdhikshith.medsafe.core

import com.shivdhikshith.medsafe.core.notifications.NotificationHelper

data class AlertNotificationRecord(
    val notificationId: Int,
    val medicineName: String,
    val batchNumber: String,
    val daysRemaining: Long
)

data class PersonalReminderRecord(
    val notificationId: Int,
    val reminderTitle: String,
    val medicineName: String
)

class FakeNotificationHelper : NotificationHelper(null) {
    val expiryAlerts = mutableListOf<AlertNotificationRecord>()
    val personalReminders = mutableListOf<PersonalReminderRecord>()
    val cancelledIds = mutableListOf<Int>()
    var shouldSucceed: Boolean = true

    override fun showExpiryAlert(
        notificationId: Int,
        medicineName: String,
        batchNumber: String,
        daysRemaining: Long
    ): Boolean {
        if (!shouldSucceed) return false
        expiryAlerts.add(
            AlertNotificationRecord(
                notificationId = notificationId,
                medicineName = medicineName,
                batchNumber = batchNumber,
                daysRemaining = daysRemaining
            )
        )
        return true
    }

    override fun showPersonalReminder(
        notificationId: Int,
        reminderTitle: String,
        medicineName: String
    ): Boolean {
        if (!shouldSucceed) return false
        personalReminders.add(
            PersonalReminderRecord(
                notificationId = notificationId,
                reminderTitle = reminderTitle,
                medicineName = medicineName
            )
        )
        return true
    }

    override fun cancelNotification(notificationId: Int) {
        cancelledIds.add(notificationId)
    }
}
