package com.shivdhikshith.medsafe.feature

import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.usecase.InventorySortType
import com.shivdhikshith.medsafe.domain.usecase.ObserveInventoryUseCase
import com.shivdhikshith.medsafe.feature.inventory.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicineRepository = FakeMedicineRepository()
        settingsRepository = FakeSettingsRepository()
        val useCase = ObserveInventoryUseCase(medicineRepository, settingsRepository)
        viewModel = InventoryViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onSearchQueryChanged_updatesUiStateQuery() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.onSearchQueryChanged("Paracetamol")
        advanceUntilIdle()

        assertEquals("Paracetamol", viewModel.uiState.value.searchQuery)
        collectJob.cancel()
    }

    @Test
    fun onStatusFilterSelected_updatesUiStateFilter() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.onStatusFilterSelected(ExpiryStatus.EXPIRING_SOON)
        advanceUntilIdle()

        assertEquals(ExpiryStatus.EXPIRING_SOON, viewModel.uiState.value.selectedStatusFilter)
        collectJob.cancel()
    }

    @Test
    fun onSortTypeSelected_updatesUiStateSort() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.onSortTypeSelected(InventorySortType.NAME_A_TO_Z)
        advanceUntilIdle()

        assertEquals(InventorySortType.NAME_A_TO_Z, viewModel.uiState.value.selectedSortType)
        collectJob.cancel()
    }
}
