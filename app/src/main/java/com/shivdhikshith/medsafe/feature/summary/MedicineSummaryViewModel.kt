package com.shivdhikshith.medsafe.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.usecase.GetMedicineDetailUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateMedicineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MedicineSummaryUiState(
    val isLoading: Boolean = true,
    val medicineWithBatches: MedicineWithBatches? = null,
    val errorMessage: String? = null
)

class MedicineSummaryViewModel(
    private val medicineId: Long,
    private val getMedicineDetailUseCase: GetMedicineDetailUseCase,
    private val updateMedicineUseCase: UpdateMedicineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineSummaryUiState())
    val uiState: StateFlow<MedicineSummaryUiState> = _uiState.asStateFlow()

    init {
        getMedicineDetailUseCase(medicineId)
            .onEach { detail ->
                _uiState.value = MedicineSummaryUiState(
                    isLoading = false,
                    medicineWithBatches = detail
                )
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load summary"
                )
            }
            .launchIn(viewModelScope)
    }

    fun updateNotes(notes: String) {
        val currentMedicine = _uiState.value.medicineWithBatches?.medicine ?: return
        viewModelScope.launch {
            try {
                updateMedicineUseCase(currentMedicine.copy(notes = notes.trim()))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to save notes")
            }
        }
    }

    class Factory(
        private val medicineId: Long,
        private val getMedicineDetailUseCase: GetMedicineDetailUseCase,
        private val updateMedicineUseCase: UpdateMedicineUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedicineSummaryViewModel(medicineId, getMedicineDetailUseCase, updateMedicineUseCase) as T
        }
    }
}
