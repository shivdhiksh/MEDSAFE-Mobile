package com.shivdhikshith.medsafe.feature.dashboard

import com.shivdhikshith.medsafe.domain.model.DashboardSummary

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary = DashboardSummary(),
    val errorMessage: String? = null
)
