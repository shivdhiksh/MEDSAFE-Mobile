package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.core.backup.BackupManager
import com.shivdhikshith.medsafe.core.backup.DataExportManager
import com.shivdhikshith.medsafe.domain.model.AlertHistory
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataManagementTest {

    @Test
    fun `exportToCsv correctly escapes commas, quotes, and newlines`() {
        val med = Medicine(
            id = 1,
            name = "Paracetamol, 500mg",
            notes = "Take with water\nStore cool"
        )
        val batch = Batch(
            id = 1,
            medicineId = 1,
            batchNumber = "B-01",
            expiryDate = 1750000000000L,
            storageLocation = "Cabinet, Top Shelf"
        )
        val data = listOf(MedicineWithBatches(med, listOf(batch)))

        val csv = DataExportManager.exportToCsv(data)
        assertTrue(csv.contains("\"Paracetamol, 500mg\""))
        assertTrue(csv.contains("\"Cabinet, Top Shelf\""))
        assertTrue(csv.contains("B-01"))
    }

    @Test
    fun `filename generation produces expected MEDSAFE_Backup_timestamp format`() {
        val timestamp = 1750000000000L
        val filename = BackupManager.generateBackupFileName(timestamp)
        assertEquals("MEDSAFE_Backup_1750000000000.json", filename)

        val currentFilename = BackupManager.generateBackupFileName()
        assertTrue(currentFilename.startsWith("MEDSAFE_Backup_"))
        assertTrue(currentFilename.endsWith(".json"))
    }

    @Test
    fun `backup json roundtrip preserves medicine, batch, and reminder counts`() {
        val med1 = Medicine(id = 1, name = "Aspirin")
        val med2 = Medicine(id = 2, name = "Cetirizine")
        val b1 = Batch(id = 1, medicineId = 1, expiryDate = 1750000000000L)
        val b2 = Batch(id = 2, medicineId = 2, expiryDate = 1760000000000L)
        val rem1 = Reminder(id = 1, medicineId = 1, title = "Morning Aspirin", timeInMillis = 28800000L)

        val inventory = listOf(
            MedicineWithBatches(med1, listOf(b1)),
            MedicineWithBatches(med2, listOf(b2))
        )
        val reminders = listOf(rem1)

        val backupJson = BackupManager.createBackupJson(inventory, reminders)
        val validation = BackupManager.validateBackup(backupJson)

        assertTrue(validation.isValid)
        assertEquals(2, validation.medicineCount)
        assertEquals(2, validation.batchCount)
        assertEquals(1, validation.reminderCount)
        assertEquals("Aspirin", validation.payload!!.medicines.first().name)
        assertEquals("Morning Aspirin", validation.payload!!.reminders.first().title)
    }

    @Test
    fun `backup json preserves alert history, user settings, and photo metadata without image blobs`() {
        val med = Medicine(id = 1, name = "Amoxicillin", strength = "250mg")
        val batch = Batch(id = 10, medicineId = 1, batchNumber = "AMX-1", expiryDate = 1750000000000L)
        val rem = Reminder(id = 20, medicineId = 1, title = "Afternoon Dose", timeInMillis = 50400000L)
        val alert = AlertHistory(id = 30, batchId = 10, alertType = "EXPIRY_WARNING_7_DAYS", sentAt = 1749000000000L)
        val photo = MedicinePhoto(id = 40, medicineId = 1, filePath = "photos/amx_box.jpg", caption = "Box front")
        val settings = UserSettings(notificationsEnabled = true, alertThresholdDays = setOf(30, 7, 1), theme = "DARK")

        val backupJson = BackupManager.createBackupJson(
            medicinesWithBatches = listOf(MedicineWithBatches(med, listOf(batch))),
            reminders = listOf(rem),
            alertHistory = listOf(alert),
            settings = settings,
            photos = listOf(photo)
        )

        // Verify valid UTF-8 JSON format
        assertTrue(backupJson.contains("\"format\": \"MEDSAFE_BACKUP\""))
        assertTrue(backupJson.contains("\"photos/amx_box.jpg\""))
        // Verify no base64 image blobs are stored
        assertFalse(backupJson.contains("data:image"))
        assertFalse(backupJson.contains("base64"))

        val validation = BackupManager.validateBackup(backupJson)
        assertTrue(validation.isValid)
        assertEquals(1, validation.medicineCount)
        assertEquals(1, validation.batchCount)
        assertEquals(1, validation.reminderCount)
        assertEquals(1, validation.alertCount)
        assertEquals(1, validation.photoCount)
        assertTrue(validation.hasSettings)

        val payload = validation.payload!!
        assertEquals("DARK", payload.settings?.theme)
        assertEquals("EXPIRY_WARNING_7_DAYS", payload.alertHistory.first().alertType)
        assertEquals("photos/amx_box.jpg", payload.photoMetadata.first().filePath)
    }

    @Test
    fun `restore payload mapping correctly maps old IDs to new IDs during restore`() {
        val medOldId = 101L
        val batchOldId = 202L
        val alertOldId = 303L

        val med = Medicine(id = medOldId, name = "Metformin")
        val batch = Batch(id = batchOldId, medicineId = medOldId, expiryDate = 1760000000000L)
        val alert = AlertHistory(id = alertOldId, batchId = batchOldId, alertType = "EXPIRY_WARNING_1_DAY")

        val backupJson = BackupManager.createBackupJson(
            medicinesWithBatches = listOf(MedicineWithBatches(med, listOf(batch))),
            reminders = emptyList(),
            alertHistory = listOf(alert)
        )

        val validation = BackupManager.validateBackup(backupJson)
        assertTrue(validation.isValid)
        val payload = validation.payload!!

        // Simulate restore ID mapping logic
        val medIdMap = mutableMapOf<Long, Long>()
        val newMedId = 501L
        payload.medicines.forEach {
            medIdMap[it.id] = newMedId
        }

        val batchIdMap = mutableMapOf<Long, Long>()
        val newBatchId = 602L
        payload.batches.forEach {
            val mappedMedId = medIdMap[it.medicineId]
            assertEquals(newMedId, mappedMedId)
            batchIdMap[it.id] = newBatchId
        }

        payload.alertHistory.forEach {
            val mappedBatchId = batchIdMap[it.batchId]
            assertEquals(newBatchId, mappedBatchId)
        }
    }

    @Test
    fun `validateBackup reports invalid for unrecognized json format`() {
        val result = BackupManager.validateBackup("{\"some\":\"other_app_data\"}")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Invalid backup format"))
    }

    @Test
    fun `validateBackup reports error for blank or corrupted json`() {
        val blankResult = BackupManager.validateBackup("")
        assertFalse(blankResult.isValid)
        assertEquals("Backup file is empty", blankResult.errorMessage)

        val corruptedResult = BackupManager.validateBackup("{invalid_json_format")
        assertFalse(corruptedResult.isValid)
        assertTrue(corruptedResult.errorMessage!!.contains("Failed to parse backup"))
    }
}
