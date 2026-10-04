package com.vehiclemaintenancepro.presentation.screen.service

import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.Vehicle

data class ServiceUiState(
    val isLoading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
    val activeVehicle: Vehicle? = null,
    val reminders: List<MaintenanceReminder> = emptyList(),
    val activities: List<ActivityLog> = emptyList(),
    val notice: String? = null,
    val errorMessage: String? = null,
)
