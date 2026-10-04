package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate

data class VehicleCreateRequest(
    val vehicleType: VehicleType,
    val brand: String,
    val model: String,
    val year: Int?,
    val licensePlate: String,
    val color: String?,
    val transmissionType: TransmissionType,
    val fuelType: FuelType,
    val odometerKm: Long,
    val note: String?,
    val taxDueDate: LocalDate? = null,
    val registrationDueDate: LocalDate? = null,
)
