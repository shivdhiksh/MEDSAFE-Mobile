package com.shivdhikshith.medsafe.feature.reminders

import com.shivdhikshith.medsafe.core.notifications.ReminderScheduler
import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeReminderRepository
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.model.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var reminderRepository: FakeReminderRepository
    private lateinit var fakeReminderScheduler: FakeReminderScheduler
    private lateinit var viewModel: RemindersViewModel

    class FakeReminderScheduler : ReminderScheduler {
        val scheduledReminders = mutableListOf<Pair<Reminder, String>>()
        val cancelledReminderIds = mutableListOf<Long>()

        override fun schedule(reminder: Reminder, medicineName: String) {
            scheduledReminders.add(reminder to medicineName)
        }

        override fun cancel(reminderId: Long) {
            cancelledReminderIds.add(reminderId)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicineRepository = FakeMedicineRepository()
        reminderRepository = FakeReminderRepository()
        fakeReminderScheduler = FakeReminderScheduler()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMedicine(
        id: Long,
        name: String,
        strength: String = "500mg"
    ): Medicine {
        return Medicine(
            id = id,
            name = name,
            strength = strength,
            medicineType = "Tablet",
            manufacturer = "Pharma Corp",
            barcode = "1234567890",
            notes = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun createBatch(
        id: Long,
        medicineId: Long,
        status: ExpiryStatus,
        disposedDate: Long? = if (status == ExpiryStatus.DISPOSED) System.currentTimeMillis() else null
    ): Batch {
        return Batch(
            id = id,
            medicineId = medicineId,
            batchNumber = "BATCH-$id",
            expiryDate = System.currentTimeMillis() + 86400000L * 30,
            quantity = 10,
            storageLocation = "Cabinet A",
            status = status,
            disposedDate = disposedDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun activeMedicinesAlreadyInInventory_appearAsSelectableMedicinesInViewModel() = runTest {
        val med1 = createMedicine(id = 1L, name = "qqqqqqq")
        val batch1 = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.EXPIRING_SOON)

        val med2 = createMedicine(id = 2L, name = "test medicine")
        val batch2 = createBatch(id = 102L, medicineId = 2L, status = ExpiryStatus.VALID)

        medicineRepository.setMedicines(
            listOf(
                MedicineWithBatches(med1, listOf(batch1)),
                MedicineWithBatches(med2, listOf(batch2))
            )
        )

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.medicines.size)
        assertTrue(state.medicines.any { it.name == "qqqqqqq" })
        assertTrue(state.medicines.any { it.name == "test medicine" })
        assertTrue("Selectable medicines must not be empty", state.medicines.isNotEmpty())
    }

    @Test
    fun disposedMedicinesInInventory_doNotAppearInSelectableMedicines() = runTest {
        val activeMed = createMedicine(id = 1L, name = "Paracetamol")
        val activeBatch = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.VALID)

        val disposedMed = createMedicine(id = 2L, name = "Disposed Drug")
        val disposedBatch = createBatch(id = 102L, medicineId = 2L, status = ExpiryStatus.DISPOSED)

        medicineRepository.setMedicines(
            listOf(
                MedicineWithBatches(activeMed, listOf(activeBatch)),
                MedicineWithBatches(disposedMed, listOf(disposedBatch))
            )
        )

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.medicines.size)
        assertEquals("Paracetamol", state.medicines.first().name)
        assertFalse(state.medicines.any { it.name == "Disposed Drug" })
    }

    @Test
    fun zeroSelectableMedicines_producesEmptyMedicinesListForNoMedicinesFoundCondition() = runTest {
        // Case 1: Completely empty inventory
        medicineRepository.setMedicines(emptyList())

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue("Medicines should be empty when inventory has no medicines", state.medicines.isEmpty())

        // Case 2: Only disposed medicines in inventory
        val disposedMed = createMedicine(id = 1L, name = "Old Disposed Med")
        val disposedBatch = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.DISPOSED)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(disposedMed, listOf(disposedBatch))))

        advanceUntilIdle()
        state = viewModel.uiState.value
        assertTrue("Medicines should remain empty when only disposed medicines exist", state.medicines.isEmpty())
    }

    @Test
    fun addReminder_savesToRepositoryAndSchedulesNotification() = runTest {
        val med = createMedicine(id = 1L, name = "Amoxicillin")
        val batch = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.VALID)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        viewModel.addReminder(
            medicineId = 1L,
            title = "Morning Dose",
            timeInMillis = 1750000000000L,
            recurrence = "DAILY"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.reminders.size)
        assertEquals("Morning Dose", state.reminders.first().title)
        assertEquals(1L, state.reminders.first().medicineId)

        assertEquals(1, fakeReminderScheduler.scheduledReminders.size)
        assertEquals("Amoxicillin", fakeReminderScheduler.scheduledReminders.first().second)
    }

    @Test
    fun toggleReminder_updatesEnabledStateAndScheduler() = runTest {
        val med = createMedicine(id = 1L, name = "Metformin")
        val batch = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.VALID)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        viewModel.addReminder(
            medicineId = 1L,
            title = "Dinner Dose",
            timeInMillis = 1750000000000L,
            recurrence = "DAILY"
        )
        advanceUntilIdle()

        val reminder = viewModel.uiState.value.reminders.first()
        assertTrue(reminder.isEnabled)

        // Toggle to disabled
        viewModel.toggleReminder(reminder)
        advanceUntilIdle()

        val disabledReminder = viewModel.uiState.value.reminders.first()
        assertFalse(disabledReminder.isEnabled)
        assertTrue(fakeReminderScheduler.cancelledReminderIds.contains(disabledReminder.id))
    }

    @Test
    fun deleteReminder_removesFromRepositoryAndCancelsSchedule() = runTest {
        val med = createMedicine(id = 1L, name = "Aspirin")
        val batch = createBatch(id = 101L, medicineId = 1L, status = ExpiryStatus.VALID)
        medicineRepository.setMedicines(listOf(MedicineWithBatches(med, listOf(batch))))

        viewModel = RemindersViewModel(reminderRepository, medicineRepository, fakeReminderScheduler)
        advanceUntilIdle()

        viewModel.addReminder(
            medicineId = 1L,
            title = "Emergency Dose",
            timeInMillis = 1750000000000L,
            recurrence = "ONCE"
        )
        advanceUntilIdle()

        val reminderId = viewModel.uiState.value.reminders.first().id
        viewModel.deleteReminder(reminderId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.reminders.isEmpty())
        assertTrue(fakeReminderScheduler.cancelledReminderIds.contains(reminderId))
    }
}
