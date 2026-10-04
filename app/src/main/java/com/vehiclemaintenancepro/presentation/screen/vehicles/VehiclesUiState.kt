package com.vehiclemaintenancepro.presentation.screen.vehicles

import com.vehiclemaintenancepro.domain.model.Vehicle

data class VehiclesUiState(
    val isLoading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
    val notice: String? = null,
    val errorMessage: String? = null,
)
