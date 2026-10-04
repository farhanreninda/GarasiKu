package com.vehiclemaintenancepro.data.local

import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MaintenanceQueriesTest {
    @Test
    fun queriesKeepCompleteTotalsAndRejectDecreasingOdometer() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, VehicleMaintenanceDatabase::class.java).build()
        try {
            val vehicleId = database.vehicleDao().insertVehicle(VehicleEntity(vehicleType = "Car", photoUri = null, brand = "Toyota", model = "Raize", year = 2024, licensePlate = "QC 1234", color = null, engineNumber = null, frameNumber = null, transmissionType = "Cvt", fuelType = "Gasoline", odometerKm = 10_000L, purchasePrice = null, purchaseDateEpochDay = null, note = null, isActive = true, isArchived = false, createdAtMillis = 1L, updatedAtMillis = 1L))
            repeat(250) {
                database.activityLogDao().insertLog(ActivityLogEntity(vehicleId = vehicleId, title = "Servis", description = "", costAmount = 100L, occurredAtMillis = 10L))
            }
            database.activityLogDao().insertLog(ActivityLogEntity(vehicleId = vehicleId, title = "Di luar periode", description = "", costAmount = 999L, occurredAtMillis = 20L))
            val expenses = database.activityLogDao().observeExpensesBetween(0L, 20L).first()
            assertEquals(250, expenses.size)
            assertEquals(25_000L, expenses.sumOf { it.costAmount ?: 0L })
            assertEquals(0, database.vehicleDao().updateOdometer(vehicleId, 9_000L, 20L))
            assertEquals(1, database.vehicleDao().updateOdometer(vehicleId, 11_000L, 20L))
            assertEquals(11_000L, database.vehicleDao().getVehicles().single().odometerKm)
        } finally {
            database.close()
        }
    }
}
