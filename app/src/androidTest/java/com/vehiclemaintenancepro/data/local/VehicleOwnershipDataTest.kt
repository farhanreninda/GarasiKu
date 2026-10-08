package com.vehiclemaintenancepro.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.local.entity.VehicleEntity
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.data.repository.VehicleRepositoryImpl
import com.vehiclemaintenancepro.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class VehicleOwnershipDataTest {
    @Test fun purchaseAndAccessoriesRemainSeparateAndUnknownPriceSurvives() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, VehicleMaintenanceDatabase::class.java).build()
        try {
            val dao = database.vehicleDao()
            val id = dao.insertVehicle(VehicleEntity(vehicleType = "Motorcycle", photoUri = null, brand = "Honda", model = "PCX 160 CBS", year = 2023,
                licensePlate = "QC OWNERSHIP", color = null, engineNumber = null, frameNumber = null, transmissionType = "Cvt", fuelType = "Gasoline",
                odometerKm = 19_537, purchasePrice = null, purchaseDateEpochDay = null, note = "Catatan tetap utuh", isActive = true, isArchived = false,
                createdAtMillis = 1, updatedAtMillis = 1))
            val repository = VehicleRepositoryImpl(database, dao, database.maintenanceReminderDao())
            val request = VehicleDetailsUpdateRequest("QCENGINE0001", "QCFRAME0000000001", 32_075_000, 31_775_000, "Dealer QC",
                LocalDate.of(2023, 5, 27), LocalDate.of(2023, 7, 13))
            repository.updateDetails(id, request)
            val hugger = VehicleAccessory(name = "Hugger + stiker", price = 84_676, status = AccessoryStatus.Installed)
            val footrest = VehicleAccessory(name = "Pijakan kaki PCX", status = AccessoryStatus.Installed)
            repository.saveAccessory(id, hugger)
            repository.saveAccessory(id, footrest)
            repository.saveAccessory(id, hugger.copy(price = 85_000))
            val saved = repository.observeVehicles().first().single()
            assertEquals(31_775_000L, saved.purchasePrice)
            assertEquals(32_075_000L, saved.otrPrice)
            assertEquals(request.orderDate, saved.orderDate)
            assertEquals(request.deliveryDate, saved.purchaseDate)
            assertEquals("Catatan tetap utuh", saved.note)
            assertEquals(2, saved.accessories.size)
            assertNull(saved.accessories.first { it.id == footrest.id }.price)
            assertEquals(85_000L, saved.accessories.sumOf { it.price ?: 0L })
            assertTrue(database.activityLogDao().observeRecentLogs(10).first().isEmpty())
            repository.deleteAccessory(id, hugger.id)
            assertEquals(listOf(footrest), dao.getVehicle(id)!!.toDomain().accessories)
        } finally { database.close() }
    }
}
