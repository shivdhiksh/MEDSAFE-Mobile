package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.usecase.InventorySortType
import com.shivdhikshith.medsafe.domain.usecase.ObserveInventoryUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ObserveInventoryUseCaseTest {

    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var useCase: ObserveInventoryUseCase

    @Before
    fun setUp() {
        medicineRepository = FakeMedicineRepository()
        settingsRepository = FakeSettingsRepository()
        useCase = ObserveInventoryUseCase(medicineRepository, settingsRepository)

        val today = LocalDate.now()

        val medA = Medicine(id = 1, name = "Amoxicillin", manufacturer = "GSK")
        val batchA = Batch(id = 11, medicineId = 1, expiryDate = DateUtils.localDateToEpochMillis(today.plusDays(40)), storageLocation = "Fridge")

        val medZ = Medicine(id = 2, name = "Zincovit", manufacturer = "Apex")
        val batchZ = Batch(id = 12, medicineId = 2, expiryDate = DateUtils.localDateToEpochMillis(today.plusDays(10)), storageLocation = "Cabinet")

        medicineRepository.setMedicines(
            listOf(
                MedicineWithBatches(medA, listOf(batchA)),
                MedicineWithBatches(medZ, listOf(batchZ))
            )
        )
    }

    @Test
    fun observeInventory_searchByName_returnsMatchedOnly() = runTest {
        val result = useCase(searchQuery = "Zinc").first()
        assertEquals(1, result.size)
        assertEquals("Zincovit", result.first().medicine.name)
    }

    @Test
    fun observeInventory_searchByLocation_returnsMatchedOnly() = runTest {
        val result = useCase(searchQuery = "Fridge").first()
        assertEquals(1, result.size)
        assertEquals("Amoxicillin", result.first().medicine.name)
    }

    @Test
    fun observeInventory_sortNearestExpiry_returnsZincovitFirst() = runTest {
        val result = useCase(sortType = InventorySortType.NEAREST_EXPIRY).first()
        assertEquals("Zincovit", result[0].medicine.name)
        assertEquals("Amoxicillin", result[1].medicine.name)
    }

    @Test
    fun observeInventory_sortNameAToZ_returnsAmoxicillinFirst() = runTest {
        val result = useCase(sortType = InventorySortType.NAME_A_TO_Z).first()
        assertEquals("Amoxicillin", result[0].medicine.name)
        assertEquals("Zincovit", result[1].medicine.name)
    }

    @Test
    fun observeInventory_filterExpiringSoon_returnsZincovitOnly() = runTest {
        val result = useCase(statusFilter = ExpiryStatus.EXPIRING_SOON).first()
        assertEquals(1, result.size)
        assertEquals("Zincovit", result.first().medicine.name)
    }
}
