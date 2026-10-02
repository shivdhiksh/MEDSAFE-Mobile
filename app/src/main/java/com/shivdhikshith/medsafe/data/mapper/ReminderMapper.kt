package com.shivdhikshith.medsafe.data.mapper

import com.shivdhikshith.medsafe.data.local.entity.ReminderEntity
import com.shivdhikshith.medsafe.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        medicineId = medicineId,
        title = title,
        timeInMillis = timeInMillis,
        recurrence = recurrence,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        medicineId = medicineId,
        title = title,
        timeInMillis = timeInMillis,
        recurrence = recurrence,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
}
