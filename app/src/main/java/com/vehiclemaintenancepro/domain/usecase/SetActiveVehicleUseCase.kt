package com.vehiclemaintenancepro.domain.usecase

import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import javax.inject.Inject

class SetActiveVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository,
) {
    suspend operator fun invoke(vehicleId: Long) {
        repository.setActiveVehicle(vehicleId)
    }
}
