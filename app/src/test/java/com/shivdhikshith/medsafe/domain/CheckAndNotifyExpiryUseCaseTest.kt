package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.core.FakeNotificationHelper
import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.data.FakeAlertHistoryRepository
import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.model.UserSettings
import com.shivdhikshith.medsafe.domain.usecase.CheckAndNotifyExpiryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CheckAndNotifyExpiryUseCaseTest {

    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var alertHistoryRepository: FakeAlertHistoryRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var notificationHelper: FakeNotificationHelper
    private lateinit var useCase: CheckAndNotifyExpiryUseCase

    private val baseDate: LocalDate = LocalDate.of(2026, 10, 1)

    @Before
    fun setUp() {
        medicineRepository = FakeMedicineRepository()
        alertHistoryRepository = FakeAlertHistoryRepository()
        settingsRepository = FakeSettingsRepository(
            UserSettings(
                notificationsEnabled = true,
                alertThresholdDays = setOf(30, 7, 1),
                notifyOnExpired = true
            )
        )
        notificationHelper = FakeNotificationHelper()
        useCase = CheckAndNotifyExpiryUseCase(
            medicineRepository = medicineRepository,
            alertHistoryRepository = alertHistoryRepository,
            settingsRepository = settingsRepository,
            notificationHelper = notificationHelper
        )
    }

    @Test
    fun `notifies 30 days before expiry and prevents duplicates`() = runTest {
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(30))
        val med = Medicine(id = 1, name = "Paracetamol")
        val batch = Batch(id = 101, medicineId = 1, batchNumber = "ABC123", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val firstResult = useCase(currentDate = baseDate)
        assertEquals(1, firstResult.notificationsSent)
        assertEquals(0, firstResult.duplicatesSuppressed)
        assertEquals(1, notificationHelper.expiryAlerts.size)
        assertEquals(30L, notificationHelper.expiryAlerts[0].daysRemaining)
        assertEquals("Paracetamol", notificationHelper.expiryAlerts[0].medicineName)
        assertEquals("ABC123", notificationHelper.expiryAlerts[0].batchNumber)

        // Verify alert history recorded
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 101, alertType = "30_DAYS"))

        // Second run must suppress duplicate
        val secondResult = useCase(currentDate = baseDate)
        assertEquals(0, secondResult.notificationsSent)
        assertEquals(1, secondResult.duplicatesSuppressed)
        assertEquals(1, notificationHelper.expiryAlerts.size) // No extra notification
    }

    @Test
    fun `notifies 7 days before expiry`() = runTest {
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(7))
        val med = Medicine(id = 2, name = "Amoxicillin")
        val batch = Batch(id = 202, medicineId = 2, batchNumber = "AMX77", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(1, result.notificationsSent)
        assertEquals(7L, notificationHelper.expiryAlerts[0].daysRemaining)
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 202, alertType = "7_DAYS"))
    }

    @Test
    fun `notifies 1 day before expiry`() = runTest {
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(1))
        val med = Medicine(id = 3, name = "Ibuprofen")
        val batch = Batch(id = 303, medicineId = 3, batchNumber = "IBU01", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(1, result.notificationsSent)
        assertEquals(1L, notificationHelper.expiryAlerts[0].daysRemaining)
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 303, alertType = "1_DAY"))
    }

    @Test
    fun `notifies when medicine is expired`() = runTest {
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.minusDays(5))
        val med = Medicine(id = 4, name = "Cetirizine")
        val batch = Batch(id = 404, medicineId = 4, batchNumber = "CET99", expiryDate = expiryDate, status = ExpiryStatus.EXPIRED)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(1, result.notificationsSent)
        assertEquals(-5L, notificationHelper.expiryAlerts[0].daysRemaining)
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 404, alertType = "EXPIRED"))

        // Re-check does not re-alert for expired batch
        val secondResult = useCase(currentDate = baseDate)
        assertEquals(0, secondResult.notificationsSent)
        assertEquals(1, secondResult.duplicatesSuppressed)
    }

    @Test
    fun `disposed batches do NOT generate expiry notifications`() = runTest {
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.minusDays(10))
        val med = Medicine(id = 5, name = "Expired Syrup")
        val batch = Batch(
            id = 505,
            medicineId = 5,
            batchNumber = "DISP01",
            expiryDate = expiryDate,
            status = ExpiryStatus.DISPOSED,
            disposedDate = DateUtils.localDateToEpochMillis(baseDate.minusDays(2))
        )
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(0, result.notificationsSent)
        assertEquals(0, result.duplicatesSuppressed)
        assertTrue(notificationHelper.expiryAlerts.isEmpty())
        assertTrue(alertHistoryRepository.alerts.isEmpty())
    }

    @Test
    fun `no notifications sent when notifications are disabled in settings`() = runTest {
        settingsRepository.setNotificationsEnabled(false)
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(7))
        val med = Medicine(id = 6, name = "Aspirin")
        val batch = Batch(id = 606, medicineId = 6, batchNumber = "ASP01", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(0, result.notificationsSent)
        assertTrue(notificationHelper.expiryAlerts.isEmpty())
    }

    @Test
    fun `batches not at threshold do not generate notifications`() = runTest {
        // 20 days remaining is not 30, 7, 1, or expired
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(20))
        val med = Medicine(id = 7, name = "Vitamin C")
        val batch = Batch(id = 707, medicineId = 7, batchNumber = "VIT20", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(0, result.notificationsSent)
        assertTrue(notificationHelper.expiryAlerts.isEmpty())
    }

    @Test
    fun `active batch expiring in exactly 1 day generates notification and records alert history when no record exists`() = runTest {
        // Arrange: active batch, expiry exactly 1 day away, notifications enabled, 1-day threshold enabled, no existing alert-history
        settingsRepository.setNotificationsEnabled(true)
        settingsRepository.setAlertThresholdDays(setOf(30, 7, 1))

        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(1))
        val med = Medicine(id = 8, name = "Paracetamol Rapid")
        val batch = Batch(
            id = 808,
            medicineId = 8,
            batchNumber = "BATCH-TOMORROW",
            expiryDate = expiryDate,
            status = ExpiryStatus.EXPIRING_SOON
        )
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        // Precondition: no existing alert-history record
        assertTrue(alertHistoryRepository.alerts.none { it.batchId == 808L })
        assertFalse(alertHistoryRepository.hasAlertBeenSent(batchId = 808L, alertType = "1_DAY"))

        // Act
        val result = useCase(currentDate = baseDate)

        // Assert: notification generated and alert history recorded only after successful dispatch
        assertEquals(1, result.batchesChecked)
        assertEquals(1, result.notificationsSent)
        assertEquals(0, result.duplicatesSuppressed)
        assertEquals(0, result.errorsCount)

        assertEquals(1, notificationHelper.expiryAlerts.size)
        val alert = notificationHelper.expiryAlerts.first()
        assertEquals(1L, alert.daysRemaining)
        assertEquals("Paracetamol Rapid", alert.medicineName)
        assertEquals("BATCH-TOMORROW", alert.batchNumber)
        assertTrue("Notification ID must be positive non-zero", alert.notificationId > 0)

        // Alert history recorded only after successful dispatch
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 808L, alertType = "1_DAY"))
        val historyRecord = alertHistoryRepository.alerts.first { it.batchId == 808L }
        assertEquals("1_DAY", historyRecord.alertType)

        // Duplicate prevention: consecutive check must suppress duplicate and not re-notify
        val secondResult = useCase(currentDate = baseDate)
        assertEquals(0, secondResult.notificationsSent)
        assertEquals(1, secondResult.duplicatesSuppressed)
        assertEquals(1, notificationHelper.expiryAlerts.size) // No extra notification
    }

    @Test
    fun `notifies on exact expiry day when daysRemaining is 0 and notifyOnExpired is enabled`() = runTest {
        // Expiry date is today (daysRemaining == 0)
        val expiryDate = DateUtils.localDateToEpochMillis(baseDate)
        val med = Medicine(id = 10, name = "Insulin")
        val batch = Batch(id = 1010, medicineId = 10, batchNumber = "INS00", expiryDate = expiryDate, status = ExpiryStatus.EXPIRING_SOON)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(1, result.notificationsSent)
        assertEquals(0L, notificationHelper.expiryAlerts[0].daysRemaining)
        assertEquals("Insulin", notificationHelper.expiryAlerts[0].medicineName)
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 1010, alertType = "EXPIRED"))

        // Duplicate suppression on second run
        val secondResult = useCase(currentDate = baseDate)
        assertEquals(0, secondResult.notificationsSent)
        assertEquals(1, secondResult.duplicatesSuppressed)
    }

    @Test
    fun `notification failure does not record alert history allowing subsequent retry`() = runTest {
        notificationHelper.shouldSucceed = false

        val expiryDate = DateUtils.localDateToEpochMillis(baseDate.plusDays(1))
        val med = Medicine(id = 9, name = "Test Med")
        val batch = Batch(id = 909, medicineId = 9, batchNumber = "B909", expiryDate = expiryDate)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        val result = useCase(currentDate = baseDate)
        assertEquals(0, result.notificationsSent)
        assertEquals(1, result.errorsCount)

        // Crucial safety: alert history must NOT be recorded if the notification failed to post
        assertFalse(alertHistoryRepository.hasAlertBeenSent(batchId = 909L, alertType = "1_DAY"))

        // When notification delivery succeeds (e.g. permission granted on reopen), alert is posted and recorded
        notificationHelper.shouldSucceed = true
        val retryResult = useCase(currentDate = baseDate)
        assertEquals(1, retryResult.notificationsSent)
        assertTrue(alertHistoryRepository.hasAlertBeenSent(batchId = 909L, alertType = "1_DAY"))
    }
}
