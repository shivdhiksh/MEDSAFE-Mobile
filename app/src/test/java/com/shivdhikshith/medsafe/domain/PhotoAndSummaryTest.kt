package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakePhotoRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.data.local.entity.PhotoEntity
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.usecase.GetMedicineDetailUseCase
import com.shivdhikshith.medsafe.domain.usecase.ObservePhotosUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateMedicineUseCase
import com.shivdhikshith.medsafe.feature.summary.MedicineSummaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoAndSummaryTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `photo mapper converts entity to domain and back preserving fields`() {
        val entity = PhotoEntity(
            id = 42L,
            medicineId = 10L,
            filePath = "/storage/emulated/0/photos/p1.jpg",
            caption = "Blister pack back",
            createdAt = 1700000000L
        )

        val domain = entity.toDomain()
        assertEquals(42L, domain.id)
        assertEquals(10L, domain.medicineId)
        assertEquals("/storage/emulated/0/photos/p1.jpg", domain.filePath)
        assertEquals("Blister pack back", domain.caption)

        val mappedEntity = domain.toEntity()
        assertEquals(entity, mappedEntity)
    }

    @Test
    fun `fake photo repository inserts and observes photos for specific medicine`() = runTest {
        val fakeRepo = FakePhotoRepository()
        val observePhotos = ObservePhotosUseCase(fakeRepo)

        val p1 = MedicinePhoto(medicineId = 1L, filePath = "/path/1.jpg", caption = "Front")
        val p2 = MedicinePhoto(medicineId = 1L, filePath = "/path/2.jpg", caption = "Back")
        val p3 = MedicinePhoto(medicineId = 2L, filePath = "/path/3.jpg", caption = "Other Med")

        val p1Id = fakeRepo.insertPhoto(p1)
        fakeRepo.insertPhoto(p2)
        fakeRepo.insertPhoto(p3)

        val med1Photos = observePhotos(1L).first()
        assertEquals(2, med1Photos.size)

        fakeRepo.deletePhoto(p1Id)
        val med1PhotosAfterDelete = observePhotos(1L).first()
        assertEquals(1, med1PhotosAfterDelete.size)
        assertEquals("Back", med1PhotosAfterDelete.first().caption)
    }

    @Test
    fun `medicine summary view model updates storage and personal notes`() = runTest(testDispatcher) {
        val fakeMedRepo = FakeMedicineRepository()
        val med = Medicine(id = 1, name = "Paracetamol", notes = "Initial note")
        val batch = Batch(medicineId = 1, expiryDate = System.currentTimeMillis() + 100_000_000L)
        val medId = fakeMedRepo.insertMedicineWithBatch(med, batch)

        val getDetail = GetMedicineDetailUseCase(fakeMedRepo, FakeSettingsRepository())
        val updateMed = UpdateMedicineUseCase(fakeMedRepo)

        val viewModel = MedicineSummaryViewModel(medId, getDetail, updateMed)
        testScheduler.advanceUntilIdle()

        viewModel.updateNotes("Store below 25°C away from humidity")
        testScheduler.advanceUntilIdle()

        val updatedDetail = fakeMedRepo.getMedicineWithBatches(medId)
        assertNotNull(updatedDetail)
        assertEquals("Store below 25°C away from humidity", updatedDetail!!.medicine.notes)
    }
}
