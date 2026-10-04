package com.vehiclemaintenancepro.domain.model

data class DashboardSummary(
    val userName: String,
    val activeVehicle: Vehicle?,
    val vehicles: List<Vehicle> = emptyList(),
    val nextService: MaintenanceReminder?,
    val nextOilChange: MaintenanceReminder?,
    val taxReminder: MaintenanceReminder?,
    val registrationReminder: MaintenanceReminder?,
    val insuranceReminder: MaintenanceReminder?,
    val monthlyExpense: Long,
    val averageFuelConsumptionKmPerLiter: Double?,
    val nearestReminder: MaintenanceReminder?,
    val alertReminderCount: Int = 0,
)
