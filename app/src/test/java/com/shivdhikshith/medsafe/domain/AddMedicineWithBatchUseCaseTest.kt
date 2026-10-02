package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.core.common.Resource
import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.usecase.AddMedicineWithBatchUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddMedicineWithBatchUseCaseTest {

    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var useCase: AddMedicineWithBatchUseCase

    @Before
    fun setUp() {
        medicineRepository = FakeMedicineRepository()
        settingsRepository = FakeSettingsRepository()
        useCase = AddMedicineWithBatchUseCase(medicineRepository, settingsRepository)
    }

    @Test
    fun invoke_validMedicineAndBatch_savesAtomicallyAndReturnsId() = runTest {
        val medicine = Medicine(name = "Ibuprofen", strength = "400mg")
        val batch = Batch(
            medicineId = 0,
            batchNumber = "B-123",
            expiryDate = System.currentTimeMillis() + 86400000L * 60, // 60 days
            quantity = 2
        )

        val result = useCase(medicine, batch)

        assertTrue(result is Resource.Success)
        val savedId = (result as Resource.Success).data
        assertEquals(1L, savedId)

        val retrieved = medicineRepository.getMedicineWithBatches(savedId)
        assertEquals("Ibuprofen", retrieved?.medicine?.name)
        assertEquals(1, retrieved?.batches?.size)
        assertEquals("B-123", retrieved?.batches?.first()?.batchNumber)
    }

    @Test
    fun invoke_invalidMedicine_returnsErrorWithoutSaving() = runTest {
        val medicine = Medicine(name = "") // invalid blank name
        val batch = Batch(
            medicineId = 0,
            expiryDate = System.currentTimeMillis() + 86400000L,
            quantity = 1
        )

        val result = useCase(medicine, batch)

        assertTrue(result is Resource.Error)
        val allMeds = medicineRepository.getMedicineWithBatches(1L)
        assertEquals(null, allMeds)
    }
}
