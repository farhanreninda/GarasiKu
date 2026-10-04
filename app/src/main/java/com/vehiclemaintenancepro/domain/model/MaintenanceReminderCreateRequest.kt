package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate

data class MaintenanceReminderCreateRequest(
    val vehicleId: Long,
    val type: ReminderType,
    val title: String,
    val dueDate: LocalDate?,
    val dueOdometerKm: Long?,
)
