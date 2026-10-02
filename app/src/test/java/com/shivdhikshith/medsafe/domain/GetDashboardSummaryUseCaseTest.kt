package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetDashboardSummaryUseCaseTest {

    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var useCase: GetDashboardSummaryUseCase

    @Before
    fun setUp() {
        medicineRepository = FakeMedicineRepository()
        settingsRepository = FakeSettingsRepository()
        useCase = GetDashboardSummaryUseCase(medicineRepository, settingsRepository)
    }

    @Test
    fun invoke_emptyDatabase_returnsZeroMetricsAndEmptyUrgentList() = runTest {
        val summary = useCase().first()

        assertEquals(0, summary.totalActive)
        assertEquals(0, summary.validCount)
        assertEquals(0, summary.expiringSoonCount)
        assertEquals(0, summary.expiredCount)
        assertTrue(summary.urgentBatches.isEmpty())
    }

    @Test
    fun invoke_withBatches_computesCorrectCountsAndSortsUrgent() = runTest {
        val today = LocalDate.now()

        val med1 = Medicine(id = 1, name = "Valid Med")
        val batchValid = Batch(
            id = 101,
            medicineId = 1,
            expiryDate = DateUtils.localDateToEpochMillis(today.plusDays(90))
        )

        val med2 = Medicine(id = 2, name = "Expiring Soon Med")
        val batchExpiring = Batch(
            id = 102,
            medicineId = 2,
            expiryDate = DateUtils.localDateToEpochMillis(today.plusDays(10))
        )

        val med3 = Medicine(id = 3, name = "Expired Med")
        val batchExpired = Batch(
            id = 103,
            medicineId = 3,
            expiryDate = DateUtils.localDateToEpochMillis(today.minusDays(5))
        )

        medicineRepository.setMedicines(
            listOf(
                MedicineWithBatches(med1, listOf(batchValid)),
                MedicineWithBatches(med2, listOf(batchExpiring)),
                MedicineWithBatches(med3, listOf(batchExpired))
            )
        )

        val summary = useCase().first()

        assertEquals(3, summary.totalActive)
        assertEquals(1, summary.validCount)
        assertEquals(1, summary.expiringSoonCount)
        assertEquals(1, summary.expiredCount)
        assertEquals(2, summary.urgentBatches.size)

        // Verify earliest expiry is first in urgent batches
        assertEquals(103L, summary.urgentBatches[0].batch.id)
        assertEquals(102L, summary.urgentBatches[1].batch.id)
    }
}
