package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeActiveVehicle(): Flow<Vehicle?>
    suspend fun addVehicle(request: VehicleCreateRequest): Long
    suspend fun setActiveVehicle(vehicleId: Long)
    suspend fun archiveVehicle(vehicleId: Long)
    suspend fun updateOdometer(vehicleId: Long, odometerKm: Long)
}
