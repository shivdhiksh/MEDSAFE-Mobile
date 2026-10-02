package com.shivdhikshith.medsafe.domain.usecase

import androidx.room.withTransaction
import com.shivdhikshith.medsafe.core.backup.BackupManager
import com.shivdhikshith.medsafe.core.backup.BackupPayload
import com.shivdhikshith.medsafe.core.backup.BackupValidationResult
import com.shivdhikshith.medsafe.core.backup.DataExportManager
import com.shivdhikshith.medsafe.data.local.database.MedSafeDatabase
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.repository.AlertHistoryRepository
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.PhotoRepository
import com.shivdhikshith.medsafe.domain.repository.ReminderRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository

class DataManagementUseCases(
    private val medicineRepository: MedicineRepository,
    private val reminderRepository: ReminderRepository,
    private val database: MedSafeDatabase,
    private val settingsRepository: SettingsRepository? = null,
    private val alertHistoryRepository: AlertHistoryRepository? = null,
    private val photoRepository: PhotoRepository? = null
) {
    suspend fun exportCsv(): String {
        val inventory = medicineRepository.getAllMedicinesWithBatches()
        return DataExportManager.exportToCsv(inventory)
    }

    suspend fun exportJson(): String {
        val inventory = medicineRepository.getAllMedicinesWithBatches()
        return BackupManager.exportInventoryJson(inventory)
    }

    suspend fun createBackup(): String {
        val inventory = medicineRepository.getAllMedicinesWithBatches()
        val reminders = reminderRepository.getAllReminders()
        val alerts = alertHistoryRepository?.getAllAlerts()
            ?: try { database.alertHistoryDao().getAllAlerts().map { it.toDomain() } } catch (_: Exception) { emptyList() }
        val settings = settingsRepository?.getSettings()
        val photos = photoRepository?.getAllPhotos()
            ?: try { database.photoDao().getAllPhotos().map { it.toDomain() } } catch (_: Exception) { emptyList() }
        return BackupManager.createBackupJson(
            medicinesWithBatches = inventory,
            reminders = reminders,
            alertHistory = alerts,
            settings = settings,
            photos = photos
        )
    }

    fun validateBackup(jsonString: String): BackupValidationResult {
        return BackupManager.validateBackup(jsonString)
    }

    suspend fun restoreBackup(payload: BackupPayload, replaceAll: Boolean) {
        database.withTransaction {
            val medDao = database.medicineDao()
            val batchDao = database.batchDao()
            val reminderDao = database.reminderDao()
            val alertDao = database.alertHistoryDao()
            val photoDao = database.photoDao()

            if (replaceAll) {
                try { alertDao.deleteAllAlerts() } catch (_: Exception) {}
                try { photoDao.deleteAllPhotos() } catch (_: Exception) {}
                val currentMedicines = medicineRepository.getAllMedicinesWithBatches()
                currentMedicines.forEach {
                    medDao.deleteMedicineById(it.medicine.id)
                }
                val currentReminders = reminderRepository.getAllReminders()
                currentReminders.forEach {
                    reminderDao.deleteReminderById(it.id)
                }
            }

            // Map old medicine ID to new medicine ID
            val medIdMap = mutableMapOf<Long, Long>()

            payload.medicines.forEach { med ->
                val newId = if (replaceAll && med.id > 0) {
                    medDao.insertMedicine(med.toEntity())
                    med.id
                } else {
                    medDao.insertMedicine(med.copy(id = 0).toEntity())
                }
                medIdMap[med.id] = newId
            }

            // Map old batch ID to new batch ID
            val batchIdMap = mutableMapOf<Long, Long>()

            payload.batches.forEach { batch ->
                val targetMedId = medIdMap[batch.medicineId] ?: batch.medicineId
                val newBatchId = if (replaceAll && batch.id > 0) {
                    batchDao.insertBatch(batch.copy(medicineId = targetMedId).toEntity())
                    batch.id
                } else {
                    batchDao.insertBatch(batch.copy(id = 0, medicineId = targetMedId).toEntity())
                }
                batchIdMap[batch.id] = newBatchId
            }

            payload.reminders.forEach { reminder ->
                val targetMedId = medIdMap[reminder.medicineId] ?: reminder.medicineId
                val remToInsert = if (replaceAll && reminder.id > 0) {
                    reminder.copy(medicineId = targetMedId).toEntity()
                } else {
                    reminder.copy(id = 0, medicineId = targetMedId).toEntity()
                }
                reminderDao.insertReminder(remToInsert)
            }

            payload.alertHistory.forEach { alert ->
                val targetBatchId = batchIdMap[alert.batchId] ?: alert.batchId
                val alertToInsert = if (replaceAll && alert.id > 0) {
                    alert.copy(batchId = targetBatchId).toEntity()
                } else {
                    alert.copy(id = 0, batchId = targetBatchId).toEntity()
                }
                alertDao.insertAlert(alertToInsert)
            }

            payload.photoMetadata.forEach { photo ->
                val targetMedId = medIdMap[photo.medicineId] ?: photo.medicineId
                val photoToInsert = if (replaceAll && photo.id > 0) {
                    photo.copy(medicineId = targetMedId).toEntity()
                } else {
                    photo.copy(id = 0, medicineId = targetMedId).toEntity()
                }
                photoDao.insertPhoto(photoToInsert)
            }
        }

        if (replaceAll && payload.settings != null && settingsRepository != null) {
            val s = payload.settings
            settingsRepository.setNotificationsEnabled(s.notificationsEnabled)
            settingsRepository.setAlertThresholdDays(s.alertThresholdDays)
            settingsRepository.setNotifyOnExpired(s.notifyOnExpired)
            settingsRepository.setTheme(s.theme)
            settingsRepository.setAlertDays(s.alertDays)
        }
    }
}
