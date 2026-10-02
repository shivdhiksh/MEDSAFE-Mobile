package com.shivdhikshith.medsafe.core.backup

import com.shivdhikshith.medsafe.domain.model.AlertHistory
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.domain.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val version: Int = 1,
    val appVersion: String = "1.0.0",
    val timestamp: Long = System.currentTimeMillis(),
    val medicines: List<Medicine> = emptyList(),
    val batches: List<Batch> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val alertHistory: List<AlertHistory> = emptyList(),
    val settings: UserSettings? = null,
    val photoMetadata: List<MedicinePhoto> = emptyList()
)

data class BackupValidationResult(
    val isValid: Boolean,
    val medicineCount: Int = 0,
    val batchCount: Int = 0,
    val reminderCount: Int = 0,
    val alertCount: Int = 0,
    val photoCount: Int = 0,
    val hasSettings: Boolean = false,
    val errorMessage: String? = null,
    val payload: BackupPayload? = null
)

object BackupManager {

    fun generateBackupFileName(timestamp: Long = System.currentTimeMillis()): String {
        return "MEDSAFE_Backup_$timestamp.json"
    }

    fun createBackupJson(
        medicinesWithBatches: List<MedicineWithBatches>,
        reminders: List<Reminder>,
        alertHistory: List<AlertHistory> = emptyList(),
        settings: UserSettings? = null,
        photos: List<MedicinePhoto> = emptyList()
    ): String {
        val root = JSONObject()
        root.put("format", "MEDSAFE_BACKUP")
        root.put("version", 1)
        root.put("appVersion", "1.0.0")
        root.put("timestamp", System.currentTimeMillis())

        val medicinesArray = JSONArray()
        val batchesArray = JSONArray()

        medicinesWithBatches.forEach { medWithBatches ->
            val med = medWithBatches.medicine
            val medObj = JSONObject()
            medObj.put("id", med.id)
            medObj.put("name", med.name)
            medObj.put("strength", med.strength)
            medObj.put("manufacturer", med.manufacturer)
            medObj.put("medicineType", med.medicineType)
            medObj.put("barcode", med.barcode)
            medObj.put("notes", med.notes)
            medObj.put("createdAt", med.createdAt)
            medObj.put("updatedAt", med.updatedAt)
            medicinesArray.put(medObj)

            medWithBatches.batches.forEach { batch ->
                val batchObj = JSONObject()
                batchObj.put("id", batch.id)
                batchObj.put("medicineId", batch.medicineId)
                batchObj.put("batchNumber", batch.batchNumber)
                batchObj.put("expiryDate", batch.expiryDate)
                batchObj.put("quantity", batch.quantity)
                batchObj.put("storageLocation", batch.storageLocation)
                batchObj.put("status", batch.status.name)
                batchObj.put("openedDate", batch.openedDate ?: JSONObject.NULL)
                batchObj.put("disposedDate", batch.disposedDate ?: JSONObject.NULL)
                batchObj.put("createdAt", batch.createdAt)
                batchObj.put("updatedAt", batch.updatedAt)
                batchesArray.put(batchObj)
            }
        }
        root.put("medicines", medicinesArray)
        root.put("batches", batchesArray)

        val remindersArray = JSONArray()
        reminders.forEach { reminder ->
            val remObj = JSONObject()
            remObj.put("id", reminder.id)
            remObj.put("medicineId", reminder.medicineId)
            remObj.put("title", reminder.title)
            remObj.put("timeInMillis", reminder.timeInMillis)
            remObj.put("recurrence", reminder.recurrence)
            remObj.put("isEnabled", reminder.isEnabled)
            remObj.put("createdAt", reminder.createdAt)
            remindersArray.put(remObj)
        }
        root.put("reminders", remindersArray)

        val alertsArray = JSONArray()
        alertHistory.forEach { alert ->
            val alertObj = JSONObject()
            alertObj.put("id", alert.id)
            alertObj.put("batchId", alert.batchId)
            alertObj.put("alertType", alert.alertType)
            alertObj.put("sentAt", alert.sentAt)
            alertsArray.put(alertObj)
        }
        root.put("alertHistory", alertsArray)

        if (settings != null) {
            val settingsObj = JSONObject()
            settingsObj.put("notificationsEnabled", settings.notificationsEnabled)
            val thresholdsArray = JSONArray()
            settings.alertThresholdDays.forEach { thresholdsArray.put(it) }
            settingsObj.put("alertThresholdDays", thresholdsArray)
            settingsObj.put("notifyOnExpired", settings.notifyOnExpired)
            settingsObj.put("theme", settings.theme)
            settingsObj.put("alertDays", settings.alertDays)
            root.put("settings", settingsObj)
        }

        val photosArray = JSONArray()
        photos.forEach { photo ->
            val photoObj = JSONObject()
            photoObj.put("id", photo.id)
            photoObj.put("medicineId", photo.medicineId)
            photoObj.put("filePath", photo.filePath)
            photoObj.put("caption", photo.caption)
            photoObj.put("createdAt", photo.createdAt)
            photosArray.put(photoObj)
        }
        root.put("photoMetadata", photosArray)

        val summaryObj = JSONObject()
        summaryObj.put("totalMedicines", medicinesWithBatches.size)
        summaryObj.put("totalBatches", medicinesWithBatches.sumOf { it.batches.size })
        summaryObj.put("totalReminders", reminders.size)
        summaryObj.put("totalAlerts", alertHistory.size)
        summaryObj.put("totalPhotos", photos.size)
        summaryObj.put("exportedAt", System.currentTimeMillis())
        root.put("summary", summaryObj)

        return root.toString(2)
    }

    fun exportInventoryJson(medicinesWithBatches: List<MedicineWithBatches>): String {
        val root = JSONObject()
        root.put("format", "MEDSAFE_INVENTORY_EXPORT")
        root.put("exportDate", System.currentTimeMillis())

        val array = JSONArray()
        medicinesWithBatches.forEach { medWithBatches ->
            val medObj = JSONObject()
            val med = medWithBatches.medicine
            medObj.put("name", med.name)
            medObj.put("strength", med.strength)
            medObj.put("form", med.medicineType)
            medObj.put("manufacturer", med.manufacturer)
            medObj.put("barcode", med.barcode)
            medObj.put("notes", med.notes)

            val batchesArray = JSONArray()
            medWithBatches.batches.forEach { batch ->
                val bObj = JSONObject()
                bObj.put("batchNumber", batch.batchNumber)
                bObj.put("expiryDateEpoch", batch.expiryDate)
                bObj.put("quantity", batch.quantity)
                bObj.put("storageLocation", batch.storageLocation)
                bObj.put("status", batch.status.name)
                batchesArray.put(bObj)
            }
            medObj.put("batches", batchesArray)
            array.put(medObj)
        }
        root.put("inventory", array)
        return root.toString(2)
    }

    fun validateBackup(jsonString: String): BackupValidationResult {
        if (jsonString.isBlank()) {
            return BackupValidationResult(isValid = false, errorMessage = "Backup file is empty")
        }
        return try {
            val root = JSONObject(jsonString)
            val format = root.optString("format")
            if (format != "MEDSAFE_BACKUP") {
                return BackupValidationResult(isValid = false, errorMessage = "Invalid backup format or incompatible file")
            }

            val version = root.optInt("version", 0)
            if (version < 1) {
                return BackupValidationResult(isValid = false, errorMessage = "Unsupported backup version")
            }

            val medicinesArray = root.optJSONArray("medicines") ?: JSONArray()
            val batchesArray = root.optJSONArray("batches") ?: JSONArray()
            val remindersArray = root.optJSONArray("reminders") ?: JSONArray()

            val medicines = mutableListOf<Medicine>()
            for (i in 0 until medicinesArray.length()) {
                val obj = medicinesArray.getJSONObject(i)
                medicines.add(
                    Medicine(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        strength = obj.optString("strength", ""),
                        manufacturer = obj.optString("manufacturer", ""),
                        medicineType = obj.optString("medicineType", ""),
                        barcode = obj.optString("barcode", ""),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val batches = mutableListOf<Batch>()
            for (i in 0 until batchesArray.length()) {
                val obj = batchesArray.getJSONObject(i)
                val statusStr = obj.optString("status", ExpiryStatus.VALID.name)
                val status = try { ExpiryStatus.valueOf(statusStr) } catch (_: Exception) { ExpiryStatus.VALID }
                batches.add(
                    Batch(
                        id = obj.optLong("id", 0),
                        medicineId = obj.getLong("medicineId"),
                        batchNumber = obj.optString("batchNumber", ""),
                        expiryDate = obj.getLong("expiryDate"),
                        quantity = obj.optInt("quantity", 1),
                        storageLocation = obj.optString("storageLocation", ""),
                        status = status,
                        openedDate = if (obj.isNull("openedDate")) null else obj.optLong("openedDate"),
                        disposedDate = if (obj.isNull("disposedDate")) null else obj.optLong("disposedDate"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val reminders = mutableListOf<Reminder>()
            for (i in 0 until remindersArray.length()) {
                val obj = remindersArray.getJSONObject(i)
                reminders.add(
                    Reminder(
                        id = obj.optLong("id", 0),
                        medicineId = obj.getLong("medicineId"),
                        title = obj.getString("title"),
                        timeInMillis = obj.getLong("timeInMillis"),
                        recurrence = obj.optString("recurrence", "DAILY"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val alertHistoryArray = root.optJSONArray("alertHistory") ?: JSONArray()
            val alertHistory = mutableListOf<AlertHistory>()
            for (i in 0 until alertHistoryArray.length()) {
                val obj = alertHistoryArray.getJSONObject(i)
                alertHistory.add(
                    AlertHistory(
                        id = obj.optLong("id", 0),
                        batchId = obj.getLong("batchId"),
                        alertType = obj.getString("alertType"),
                        sentAt = obj.optLong("sentAt", System.currentTimeMillis())
                    )
                )
            }

            val photoMetadataArray = root.optJSONArray("photoMetadata") ?: root.optJSONArray("photos") ?: JSONArray()
            val photos = mutableListOf<MedicinePhoto>()
            for (i in 0 until photoMetadataArray.length()) {
                val obj = photoMetadataArray.getJSONObject(i)
                photos.add(
                    MedicinePhoto(
                        id = obj.optLong("id", 0),
                        medicineId = obj.getLong("medicineId"),
                        filePath = obj.getString("filePath"),
                        caption = obj.optString("caption", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val settingsObj = root.optJSONObject("settings")
            val settings = settingsObj?.let { obj ->
                val thresholdsArray = obj.optJSONArray("alertThresholdDays")
                val thresholds = if (thresholdsArray != null) {
                    val set = mutableSetOf<Int>()
                    for (k in 0 until thresholdsArray.length()) {
                        set.add(thresholdsArray.getInt(k))
                    }
                    set
                } else {
                    UserSettings.DEFAULT_THRESHOLDS
                }
                UserSettings(
                    notificationsEnabled = obj.optBoolean("notificationsEnabled", true),
                    alertThresholdDays = thresholds,
                    notifyOnExpired = obj.optBoolean("notifyOnExpired", true),
                    theme = obj.optString("theme", "SYSTEM"),
                    alertDays = obj.optInt("alertDays", 30)
                )
            }

            val payload = BackupPayload(
                version = version,
                appVersion = root.optString("appVersion", "1.0.0"),
                timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                medicines = medicines,
                batches = batches,
                reminders = reminders,
                alertHistory = alertHistory,
                settings = settings,
                photoMetadata = photos
            )

            BackupValidationResult(
                isValid = true,
                medicineCount = medicines.size,
                batchCount = batches.size,
                reminderCount = reminders.size,
                alertCount = alertHistory.size,
                photoCount = photos.size,
                hasSettings = settings != null,
                payload = payload
            )
        } catch (e: Exception) {
            BackupValidationResult(isValid = false, errorMessage = "Failed to parse backup: ${e.message}")
        }
    }
}
