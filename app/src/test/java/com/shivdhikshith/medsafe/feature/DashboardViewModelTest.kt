package com.shivdhikshith.medsafe.feature

import com.shivdhikshith.medsafe.data.FakeMedicineRepository
import com.shivdhikshith.medsafe.data.FakeSettingsRepository
import com.shivdhikshith.medsafe.domain.usecase.GetDashboardSummaryUseCase
import com.shivdhikshith.medsafe.feature.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicineRepository: FakeMedicineRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicineRepository = FakeMedicineRepository()
        settingsRepository = FakeSettingsRepository()
        val useCase = GetDashboardSummaryUseCase(medicineRepository, settingsRepository)
        viewModel = DashboardViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyLoadsSummaryFromUseCase() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.summary.totalActive)
        assertEquals(null, state.errorMessage)
    }
}
