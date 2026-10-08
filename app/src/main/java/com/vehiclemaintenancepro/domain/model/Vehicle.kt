package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate

data class Vehicle(
    val id: Long,
    val vehicleType: VehicleType,
    val photoUri: String?,
    val brand: String,
    val model: String,
    val year: Int?,
    val licensePlate: String,
    val color: String?,
    val engineNumber: String?,
    val frameNumber: String?,
    val transmissionType: TransmissionType,
    val fuelType: FuelType,
    val odometerKm: Long,
    val purchasePrice: Long?,
    val purchaseDate: LocalDate?,
    val note: String?,
    val isActive: Boolean,
    val otrPrice: Long? = null,
    val dealer: String? = null,
    val orderDate: LocalDate? = null,
    val accessories: List<VehicleAccessory> = emptyList(),
)
