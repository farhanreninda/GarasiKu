package com.vehiclemaintenancepro.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.mapper.MaintenanceWorkItemJson
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MaintenanceMigrationTest {
    @Test fun migrationPreservesOldRecordsAndStructuredItemsRoundTrip() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-qc-${System.nanoTime()}.db"
        context.openOrCreateDatabase(name, 0, null).use { db ->
            db.execSQL("CREATE TABLE vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicle_type TEXT NOT NULL, photo_uri TEXT, brand TEXT NOT NULL, model TEXT NOT NULL, year INTEGER, license_plate TEXT NOT NULL, color TEXT, engine_number TEXT, frame_number TEXT, transmission_type TEXT NOT NULL, fuel_type TEXT NOT NULL, odometer_km INTEGER NOT NULL, purchase_price INTEGER, purchase_date_epoch_day INTEGER, note TEXT, is_active INTEGER NOT NULL, is_archived INTEGER NOT NULL, created_at_millis INTEGER NOT NULL, updated_at_millis INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX index_vehicles_license_plate ON vehicles(license_plate)")
            db.execSQL("CREATE TABLE maintenance_reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicle_id INTEGER NOT NULL, type TEXT NOT NULL, title TEXT NOT NULL, due_date_epoch_day INTEGER, due_time_second_of_day INTEGER, due_odometer_km INTEGER, is_completed INTEGER NOT NULL, created_at_millis INTEGER NOT NULL, updated_at_millis INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX index_maintenance_reminders_vehicle_id_type_is_completed ON maintenance_reminders(vehicle_id,type,is_completed)")
            db.execSQL("CREATE TABLE activity_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicle_id INTEGER, title TEXT NOT NULL, description TEXT, cost_amount INTEGER, occurred_at_millis INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX index_activity_logs_vehicle_id_occurred_at_millis ON activity_logs(vehicle_id,occurred_at_millis)")
            db.execSQL("INSERT INTO activity_logs(vehicle_id,title,description,cost_amount,occurred_at_millis) VALUES(1,'Oli lama','Catatan asli',64500,1)")
            db.execSQL("INSERT INTO vehicles(vehicle_type,brand,model,license_plate,transmission_type,fuel_type,odometer_km,purchase_price,note,is_active,is_archived,created_at_millis,updated_at_millis) VALUES('Motorcycle','Honda','PCX','QC MIGRATION','Cvt','Gasoline',19537,31775000,'Catatan asli',1,0,1,1)")
            db.version = 3
        }
        val room = Room.databaseBuilder(context, VehicleMaintenanceDatabase::class.java, name)
            .addMigrations(VehicleMaintenanceDatabase.MIGRATION_3_4, VehicleMaintenanceDatabase.MIGRATION_4_5).build()
        try {
            val old = room.activityLogDao().observeRecentLogsForVehicle(1, 10).first().single().toDomain()
            assertEquals("Catatan asli", old.description)
            assertEquals(64_500L, old.costAmount)
            assertNull(old.category)
            assertTrue(old.workItems.isEmpty())
            val vehicle = room.vehicleDao().observeVehicles().first().single().toDomain()
            assertEquals(31_775_000L, vehicle.purchasePrice)
            assertEquals("Catatan asli", vehicle.note)
            assertNull(vehicle.otrPrice)
            assertNull(vehicle.orderDate)
            assertTrue(vehicle.accessories.isEmpty())
            val items = listOf(MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace, "Shell 10W-40", 2_000, 3))
            assertEquals(items, MaintenanceWorkItemJson.decode(MaintenanceWorkItemJson.encode(items)))
        } finally {
            room.close()
            context.deleteDatabase(name)
        }
    }
}
