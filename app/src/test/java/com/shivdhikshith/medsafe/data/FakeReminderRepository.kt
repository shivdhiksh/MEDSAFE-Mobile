package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class FakeReminderRepository : ReminderRepository {

    private val idCounter = AtomicLong(1L)
    private val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())

    override fun observeRemindersForMedicine(medicineId: Long): Flow<List<Reminder>> =
        remindersFlow.map { list -> list.filter { it.medicineId == medicineId } }

    override fun observeAllReminders(): Flow<List<Reminder>> = remindersFlow

    override suspend fun getReminderById(reminderId: Long): Reminder? =
        remindersFlow.value.find { it.id == reminderId }

    override suspend fun getActiveReminders(): List<Reminder> =
        remindersFlow.value.filter { it.isEnabled }

    override suspend fun getAllReminders(): List<Reminder> = remindersFlow.value

    override suspend fun insertReminder(reminder: Reminder): Long {
        val newId = idCounter.getAndIncrement()
        val saved = reminder.copy(id = newId)
        remindersFlow.value = remindersFlow.value + saved
        return newId
    }

    override suspend fun updateReminder(reminder: Reminder) {
        remindersFlow.value = remindersFlow.value.map {
            if (it.id == reminder.id) reminder else it
        }
    }

    override suspend fun updateReminderEnabled(reminderId: Long, isEnabled: Boolean) {
        remindersFlow.value = remindersFlow.value.map {
            if (it.id == reminderId) it.copy(isEnabled = isEnabled) else it
        }
    }

    override suspend fun deleteReminder(reminderId: Long) {
        remindersFlow.value = remindersFlow.value.filter { it.id != reminderId }
    }

    fun setReminders(reminders: List<Reminder>) {
        remindersFlow.value = reminders
    }
}
