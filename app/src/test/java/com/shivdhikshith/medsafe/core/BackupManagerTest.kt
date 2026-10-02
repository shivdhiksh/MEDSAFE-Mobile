package com.shivdhikshith.medsafe.core

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

class BackupManagerTest {

    @Test
    fun `exportToCsv outputs RFC-4180 headers and properly escaped medicine data`() {
        val med = Medicine(id = 1, name = "Paracetamol, \"Extra\"", strength = "500mg", manufacturer = "GSK")
        val batch = Batch(id = 1, medicineId = 1, batchNumber = "BT-01", expiryDate = 1750000000000L, quantity = 2, storageLocation = "Fridge")
        val list = listOf(MedicineWithBatches(med, listOf(batch)))

        val csv = DataExportManager.exportToCsv(list)
        assertTrue(csv.startsWith("Medicine Name,Strength,Form / Type"))
        assertTrue(csv.contains("\"Paracetamol, \"\"Extra\"\"\""))
        assertTrue(csv.contains("500mg"))
        assertTrue(csv.contains("BT-01"))
    }

    @Test
    fun `generateBackupFileName produces standard filename pattern`() {
        val fixedTime = 1759999999000L
        val filename = BackupManager.generateBackupFileName(fixedTime)
        assertEquals("MEDSAFE_Backup_1759999999000.json", filename)

        val autoFilename = BackupManager.generateBackupFileName()
        assertTrue(autoFilename.startsWith("MEDSAFE_Backup_"))
        assertTrue(autoFilename.endsWith(".json"))
    }

    @Test
    fun `createBackupJson and validateBackup roundtrip correctly`() {
        val med = Medicine(id = 10, name = "Ibuprofen", strength = "400mg")
        val batch = Batch(id = 20, medicineId = 10, batchNumber = "B-400", expiryDate = 1760000000000L, quantity = 5)
        val reminder = Reminder(id = 30, medicineId = 10, title = "Take with food", timeInMillis = 28800000L)
        val alert = AlertHistory(id = 40, batchId = 20, alertType = "EXPIRY_WARNING_30_DAYS", sentAt = 1750000000000L)
        val photo = MedicinePhoto(id = 50, medicineId = 10, filePath = "photos/med_10.jpg", caption = "Package Back")
        val settings = UserSettings(notificationsEnabled = false, alertThresholdDays = setOf(14, 7), theme = "LIGHT")

        val json = BackupManager.createBackupJson(
            medicinesWithBatches = listOf(MedicineWithBatches(med, listOf(batch))),
            reminders = listOf(reminder),
            alertHistory = listOf(alert),
            settings = settings,
            photos = listOf(photo)
        )

        assertTrue(json.contains("\"format\": \"MEDSAFE_BACKUP\""))
        assertTrue(json.contains("\"photos/med_10.jpg\""))
        assertFalse(json.contains("data:image"))

        val validation = BackupManager.validateBackup(json)
        assertTrue(validation.isValid)
        assertEquals(1, validation.medicineCount)
        assertEquals(1, validation.batchCount)
        assertEquals(1, validation.reminderCount)
        assertEquals(1, validation.alertCount)
        assertEquals(1, validation.photoCount)
        assertTrue(validation.hasSettings)

        assertNotNull(validation.payload)
        val payload = validation.payload!!
        assertEquals("Ibuprofen", payload.medicines.first().name)
        assertEquals("B-400", payload.batches.first().batchNumber)
        assertEquals("Take with food", payload.reminders.first().title)
        assertEquals("EXPIRY_WARNING_30_DAYS", payload.alertHistory.first().alertType)
        assertEquals("photos/med_10.jpg", payload.photoMetadata.first().filePath)
        assertEquals("LIGHT", payload.settings?.theme)
        assertEquals(setOf(14, 7), payload.settings?.alertThresholdDays)
    }

    @Test
    fun `validateBackup returns error for empty or invalid json`() {
        val emptyResult = BackupManager.validateBackup("")
        assertFalse(emptyResult.isValid)
        assertEquals("Backup file is empty", emptyResult.errorMessage)

        val invalidResult = BackupManager.validateBackup("{\"some\":\"random_json\"}")
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.errorMessage!!.contains("Invalid backup format"))

        val badVersionResult = BackupManager.validateBackup("{\"format\":\"MEDSAFE_BACKUP\",\"version\":0}")
        assertFalse(badVersionResult.isValid)
        assertTrue(badVersionResult.errorMessage!!.contains("Unsupported backup version"))

        val malformedResult = BackupManager.validateBackup("{not a valid json")
        assertFalse(malformedResult.isValid)
        assertTrue(malformedResult.errorMessage!!.contains("Failed to parse backup"))
    }
}
