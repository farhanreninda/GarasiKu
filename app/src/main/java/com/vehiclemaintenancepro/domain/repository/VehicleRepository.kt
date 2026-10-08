package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.model.VehicleAccessory
import com.vehiclemaintenancepro.domain.model.VehicleDetailsUpdateRequest
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    suspend fun updateDetails(vehicleId: Long, request: VehicleDetailsUpdateRequest)
    suspend fun saveAccessory(vehicleId: Long, item: VehicleAccessory)
    suspend fun deleteAccessory(vehicleId: Long, accessoryId: String)
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeActiveVehicle(): Flow<Vehicle?>
    suspend fun addVehicle(request: VehicleCreateRequest): Long
    suspend fun setActiveVehicle(vehicleId: Long)
    suspend fun archiveVehicle(vehicleId: Long)
    suspend fun updateOdometer(vehicleId: Long, odometerKm: Long)
}
