package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.usecase.AddBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeleteBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateMedicineUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class MedicineManagementTest {

    private lateinit var fakeMedicineRepository: FakeMedicineRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository

    private lateinit var updateMedicineUseCase: UpdateMedicineUseCase
    private lateinit var addBatchUseCase: AddBatchUseCase
    private lateinit var updateBatchUseCase: UpdateBatchUseCase
    private lateinit var deleteBatchUseCase: DeleteBatchUseCase

    @Before
    fun setUp() {
        fakeMedicineRepository = FakeMedicineRepository()
        fakeSettingsRepository = FakeSettingsRepository()

        updateMedicineUseCase = UpdateMedicineUseCase(fakeMedicineRepository)
        addBatchUseCase = AddBatchUseCase(fakeMedicineRepository, fakeSettingsRepository)
        updateBatchUseCase = UpdateBatchUseCase(fakeMedicineRepository, fakeSettingsRepository)
        deleteBatchUseCase = DeleteBatchUseCase(fakeMedicineRepository)
    }

    @Test
    fun `update medicine updates name and details successfully`() = runTest {
        val initialMed = Medicine(id = 1, name = "Amoxicillin", strength = "250mg")
        val initialBatch = Batch(medicineId = 1, expiryDate = System.currentTimeMillis() + 100_000_000L)
        val medId = fakeMedicineRepository.insertMedicineWithBatch(initialMed, initialBatch)

        val updated = Medicine(
            id = medId,
            name = "Amoxicillin Trihydrate",
            strength = "500mg",
            notes = "Store in dry place"
        )
        updateMedicineUseCase(updated)

        val result = fakeMedicineRepository.getMedicineWithBatches(medId)
        assertNotNull(result)
        assertEquals("Amoxicillin Trihydrate", result!!.medicine.name)
        assertEquals("500mg", result.medicine.strength)
        assertEquals("Store in dry place", result.medicine.notes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update medicine with blank name throws exception`() = runTest {
        val med = Medicine(id = 1, name = "   ")
        updateMedicineUseCase(med)
    }

    @Test
    fun `add batch adds second batch to existing medicine`() = runTest {
        val med = Medicine(id = 1, name = "Paracetamol")
        val b1 = Batch(medicineId = 1, batchNumber = "B1", expiryDate = System.currentTimeMillis() + 100_000_000L)
        val medId = fakeMedicineRepository.insertMedicineWithBatch(med, b1)

        val futureExpiry = LocalDate.now().plusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val b2 = Batch(medicineId = medId, batchNumber = "B2", expiryDate = futureExpiry, quantity = 5, storageLocation = "Cabinet")
        val b2Id = addBatchUseCase(b2)

        val result = fakeMedicineRepository.getMedicineWithBatches(medId)
        assertNotNull(result)
        assertEquals(2, result!!.batches.size)
        val savedB2 = result.batches.find { it.id == b2Id }
        assertNotNull(savedB2)
        assertEquals("B2", savedB2!!.batchNumber)
        assertEquals(5, savedB2.quantity)
        assertEquals(ExpiryStatus.VALID, savedB2.status)
    }

    @Test
    fun `update batch modifies quantity and location`() = runTest {
        val med = Medicine(id = 1, name = "Cetirizine")
        val b1 = Batch(medicineId = 1, batchNumber = "B1", expiryDate = System.currentTimeMillis() + 100_000_000L, quantity = 10)
        val medId = fakeMedicineRepository.insertMedicineWithBatch(med, b1)

        val savedBatch = fakeMedicineRepository.getMedicineWithBatches(medId)!!.batches.first()
        val modifiedBatch = savedBatch.copy(quantity = 2, storageLocation = "Travel Kit")
        updateBatchUseCase(modifiedBatch)

        val result = fakeMedicineRepository.getMedicineWithBatches(medId)!!.batches.first()
        assertEquals(2, result.quantity)
        assertEquals("Travel Kit", result.storageLocation)
    }

    @Test
    fun `delete batch removes only specified batch`() = runTest {
        val med = Medicine(id = 1, name = "Ibuprofen")
        val b1 = Batch(medicineId = 1, batchNumber = "B1", expiryDate = System.currentTimeMillis() + 100_000_000L)
        val medId = fakeMedicineRepository.insertMedicineWithBatch(med, b1)

        val b2 = Batch(medicineId = medId, batchNumber = "B2", expiryDate = System.currentTimeMillis() + 200_000_000L)
        val b2Id = addBatchUseCase(b2)

        deleteBatchUseCase(b2Id)

        val result = fakeMedicineRepository.getMedicineWithBatches(medId)
        assertNotNull(result)
        assertEquals(1, result!!.batches.size)
        assertNull(result.batches.find { it.id == b2Id })
    }
}
