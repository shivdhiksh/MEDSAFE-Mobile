package com.shivdhikshith.medsafe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shivdhikshith.medsafe.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId ORDER BY timeInMillis ASC")
    fun observeRemindersForMedicine(medicineId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY timeInMillis ASC")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :reminderId")
    suspend fun updateReminderEnabled(reminderId: Long, isEnabled: Boolean)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)
}
