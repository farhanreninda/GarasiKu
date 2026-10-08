package com.vehiclemaintenancepro.presentation.screen.notifications

import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.Vehicle

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val activeVehicle: Vehicle? = null,
    val reminders: List<MaintenanceReminder> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val errorMessage: String? = null,
)
