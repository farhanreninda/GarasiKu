package com.vehiclemaintenancepro.domain.model

data class MaintenanceRecommendation(
    val title: String,
    val description: String,
    val type: ReminderType,
    val dueOdometerKm: Long?,
    val dueDate: java.time.LocalDate? = null,
    val component: MaintenanceComponent? = null,
    val lastActivityId: Long? = null,
)
