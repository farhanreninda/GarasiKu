package com.vehiclemaintenancepro.data.repository

import androidx.room.withTransaction
import com.vehiclemaintenancepro.data.local.dao.MaintenanceReminderDao
import com.vehiclemaintenancepro.data.local.dao.VehicleDao
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.data.mapper.toEntity
import com.vehiclemaintenancepro.data.mapper.VehicleAccessoryJson
import com.vehiclemaintenancepro.domain.model.VehicleAccessory
import com.vehiclemaintenancepro.domain.model.VehicleDetailsUpdateRequest
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import com.vehiclemaintenancepro.domain.model.ReminderType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val database: VehicleMaintenanceDatabase,
    private val vehicleDao: VehicleDao,
    private val reminderDao: MaintenanceReminderDao,
) : VehicleRepository {
    override suspend fun updateDetails(vehicleId: Long, request: VehicleDetailsUpdateRequest) {
        request.validate()
        check(vehicleDao.updateDetails(vehicleId, request.engineNumber?.trim()?.takeIf { it.isNotBlank() },
            request.frameNumber?.trim()?.takeIf { it.isNotBlank() }, request.otrPrice, request.purchasePrice,
            request.dealer?.trim()?.takeIf { it.isNotBlank() }, request.orderDate?.toEpochDay(),
            request.deliveryDate?.toEpochDay(), System.currentTimeMillis()) == 1) { "Kendaraan tidak tersedia" }
    }

    override suspend fun saveAccessory(vehicleId: Long, item: VehicleAccessory) = database.withTransaction {
        require(item.name.isNotBlank()) { "Nama aksesori wajib diisi" }
        require(item.price == null || item.price >= 0) { "Harga aksesori tidak valid" }
        val vehicle = checkNotNull(vehicleDao.getVehicle(vehicleId)) { "Kendaraan tidak tersedia" }
        val items = VehicleAccessoryJson.decode(vehicle.accessoriesJson)
        val updated = if (items.any { it.id == item.id }) items.map { if (it.id == item.id) item.copy(name = item.name.trim()) else it }
            else items + item.copy(name = item.name.trim())
        check(vehicleDao.updateAccessories(vehicleId, VehicleAccessoryJson.encode(updated), System.currentTimeMillis()) == 1)
    }

    override suspend fun deleteAccessory(vehicleId: Long, accessoryId: String) = database.withTransaction {
        val vehicle = checkNotNull(vehicleDao.getVehicle(vehicleId)) { "Kendaraan tidak tersedia" }
        val updated = VehicleAccessoryJson.decode(vehicle.accessoriesJson).filterNot { it.id == accessoryId }
        check(vehicleDao.updateAccessories(vehicleId, VehicleAccessoryJson.encode(updated), System.currentTimeMillis()) == 1)
    }

    override suspend fun updateOdometer(vehicleId: Long, odometerKm: Long) {
        require(odometerKm >= 0L) { "Odometer tidak boleh negatif." }
        check(vehicleDao.updateOdometer(vehicleId, odometerKm, System.currentTimeMillis()) == 1) {
            "Odometer tidak boleh lebih kecil dari catatan terakhir, dan kendaraan harus tersedia."
        }
    }

    override fun observeVehicles(): Flow<List<Vehicle>> = vehicleDao
        .observeVehicles()
        .map { vehicles -> vehicles.map { it.toDomain() } }

    override fun observeActiveVehicle(): Flow<Vehicle?> = vehicleDao
        .observeActiveVehicle()
        .map { it?.toDomain() }

    override suspend fun addVehicle(request: VehicleCreateRequest): Long = database.withTransaction {
        val nowMillis = System.currentTimeMillis()
        val shouldBeActive = vehicleDao.countVehicles() == 0
        if (shouldBeActive) {
            vehicleDao.clearActiveVehicles(updatedAtMillis = nowMillis)
        }

        val vehicleId = vehicleDao.insertVehicle(
            request.toEntity(
                isActive = shouldBeActive,
                nowMillis = nowMillis,
            ),
        )
        request.initialDocumentReminders(vehicleId).forEach { reminderRequest ->
            reminderDao.insertReminder(reminderRequest.toEntity(nowMillis = nowMillis))
        }
        vehicleId
    }

    override suspend fun setActiveVehicle(vehicleId: Long) {
        database.withTransaction {
            val nowMillis = System.currentTimeMillis()
            vehicleDao.clearActiveVehicles(updatedAtMillis = nowMillis)
            vehicleDao.markVehicleActive(
                vehicleId = vehicleId,
                updatedAtMillis = nowMillis,
            )
        }
    }

    override suspend fun archiveVehicle(vehicleId: Long) {
        database.withTransaction {
            val nowMillis = System.currentTimeMillis()
            vehicleDao.archiveVehicle(
                vehicleId = vehicleId,
                updatedAtMillis = nowMillis,
            )
            if (vehicleDao.getActiveVehicleId() == null) {
                vehicleDao.getLatestVehicleId()?.let { nextVehicleId ->
                    vehicleDao.markVehicleActive(
                        vehicleId = nextVehicleId,
                        updatedAtMillis = nowMillis,
                    )
                }
            }
        }
    }

    private fun VehicleCreateRequest.initialDocumentReminders(vehicleId: Long): List<MaintenanceReminderCreateRequest> =
        listOfNotNull(
            taxDueDate?.let { dueDate ->
                MaintenanceReminderCreateRequest(
                    vehicleId = vehicleId,
                    type = ReminderType.Tax,
                    title = "Bayar pajak kendaraan",
                    dueDate = dueDate,
                    dueOdometerKm = null,
                )
            },
            registrationDueDate?.let { dueDate ->
                MaintenanceReminderCreateRequest(
                    vehicleId = vehicleId,
                    type = ReminderType.Registration,
                    title = "Perpanjang STNK",
                    dueDate = dueDate,
                    dueOdometerKm = null,
                )
            },
        )
}
