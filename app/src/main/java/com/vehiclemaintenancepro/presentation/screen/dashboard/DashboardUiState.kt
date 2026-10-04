package com.vehiclemaintenancepro.presentation.screen.dashboard

import com.vehiclemaintenancepro.domain.model.DashboardSummary

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val errorMessage: String? = null,
)
