package com.shivdhikshith.medsafe.data.repository

import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import com.shivdhikshith.medsafe.data.local.dao.ReminderDao
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ReminderRepository {

    override fun observeRemindersForMedicine(medicineId: Long): Flow<List<Reminder>> {
        return reminderDao.observeRemindersForMedicine(medicineId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun observeAllReminders(): Flow<List<Reminder>> {
        return reminderDao.observeAllReminders()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getReminderById(reminderId: Long): Reminder? = withContext(dispatchers.io) {
        reminderDao.getReminderById(reminderId)?.toDomain()
    }

    override suspend fun getActiveReminders(): List<Reminder> = withContext(dispatchers.io) {
        reminderDao.getActiveReminders().map { it.toDomain() }
    }

    override suspend fun getAllReminders(): List<Reminder> = withContext(dispatchers.io) {
        reminderDao.getAllReminders().map { it.toDomain() }
    }

    override suspend fun insertReminder(reminder: Reminder): Long = withContext(dispatchers.io) {
        reminderDao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) = withContext(dispatchers.io) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    override suspend fun updateReminderEnabled(reminderId: Long, isEnabled: Boolean) =
        withContext(dispatchers.io) {
            reminderDao.updateReminderEnabled(reminderId, isEnabled)
        }

    override suspend fun deleteReminder(reminderId: Long) = withContext(dispatchers.io) {
        reminderDao.deleteReminderById(reminderId)
    }
}
