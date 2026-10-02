package com.shivdhikshith.medsafe.domain.repository

import com.shivdhikshith.medsafe.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeRemindersForMedicine(medicineId: Long): Flow<List<Reminder>>
    fun observeAllReminders(): Flow<List<Reminder>>
    suspend fun getReminderById(reminderId: Long): Reminder?
    suspend fun getActiveReminders(): List<Reminder>
    suspend fun getAllReminders(): List<Reminder>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun updateReminderEnabled(reminderId: Long, isEnabled: Boolean)
    suspend fun deleteReminder(reminderId: Long)
}
