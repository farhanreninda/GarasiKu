package com.vehiclemaintenancepro.presentation.screen.statistics

import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.Vehicle

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: Long? = null,
    val reminders: List<MaintenanceReminder> = emptyList(),
    val activities: List<ActivityLog> = emptyList(),
    val errorMessage: String? = null,
)
