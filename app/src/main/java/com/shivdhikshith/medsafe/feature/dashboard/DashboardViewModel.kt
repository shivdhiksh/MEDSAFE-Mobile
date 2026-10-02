package com.shivdhikshith.medsafe.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardSummary()
    }

    private fun observeDashboardSummary() {
        getDashboardSummaryUseCase()
            .onEach { summary ->
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    summary = summary,
                    errorMessage = null
                )
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load dashboard metrics"
                )
            }
            .launchIn(viewModelScope)
    }

    class Factory(
        private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(getDashboardSummaryUseCase) as T
        }
    }
}
