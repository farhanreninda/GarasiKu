package com.vehiclemaintenancepro.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.data.backup.*
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.local.entity.*
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class DataBackupTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun vehicle(id: Long = 1, archived: Boolean = false) = VehicleEntity(
        id, "Motorcycle", null, "Honda", "PCX", 2023, "QC $id", null, "ENGINE", "FRAME", "Cvt", "Gasoline",
        19537, 31775000, 19551, null, !archived, archived, 1, 2, 32075000, "Dealer", 19504,
        """[{"id":"a","name":"Hugger","price":84676,"status":"Installed"},{"id":"b","name":"Jok","status":"Unspecified"}]""")
    private fun sample() = DataBackup(1000, AppSettings("Pemilik QC", ThemeMode.Dark, false, 30, 3), listOf(vehicle(), vehicle(2, true)),
        listOf(ActivityLogEntity(7, 1, "Servis", null, null, 100, "Service", 19537, "Bengkel",
            """[{"component":"EngineOil","action":"Replace","description":"Oli","intervalKm":2000}]""")),
        listOf(MaintenanceReminderEntity(9, 2, "Service", "Selesai", 20000, 3600, 21000, true, 1, 2)))
    private class Preferences : SettingsRepository {
        val state = MutableStateFlow(AppSettings())
        var fail = false
        override suspend fun updateNotificationSettings(enabled: Boolean?, taxDays: Int?, serviceDays: Int?) {
            state.value = state.value.copy(notificationsEnabled = enabled ?: state.value.notificationsEnabled,
                taxReminderDays = taxDays ?: state.value.taxReminderDays, serviceReminderDays = serviceDays ?: state.value.serviceReminderDays)
        }
        override fun observeSettings() = state
        override suspend fun updateSettings(settings: AppSettings) { check(!fail); state.value = settings }
        override suspend fun updateUserName(userName: String) { state.value = state.value.copy(userName = userName) }
        override suspend fun updateThemeMode(themeMode: ThemeMode) { state.value = state.value.copy(themeMode = themeMode) }
    }

    @Test fun restoreRoundTripPreservesArchivedCompletedUnknownPricesAndIds() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, VehicleMaintenanceDatabase::class.java).build()
        try {
            val preferences = Preferences()
            val manager = DataBackupManager(db, preferences, context)
            val original = sample()
            assertTrue(manager.restore(BackupJson.decode(BackupJson.encode(original))))
            val restored = manager.snapshot()
            assertEquals(original.vehicles, restored.vehicles)
            assertEquals(original.activities, restored.activities)
            assertEquals(original.reminders, restored.reminders)
            assertEquals(original.settings, restored.settings)
            assertEquals(4, restored.accessoryCount)
            assertTrue(manager.restore(original))
            assertEquals(2, db.vehicleDao().getAllForBackup().size)
            assertTrue(db.vehicleDao().insertVehicle(vehicle(0).copy(licensePlate = "NEW", isActive = false)) > 2)
        } finally { db.close() }
    }

    @Test fun invalidFileIsRejectedBeforeDataChanges() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, VehicleMaintenanceDatabase::class.java).build()
        try {
            val manager = DataBackupManager(db, Preferences(), context)
            manager.restore(sample())
            val mutations: List<(JSONObject) -> Unit> = listOf(
                { it.put("version", 99) },
                { it.getJSONArray("vehicles").getJSONObject(0).put("odometerKm", 1.5) },
                { it.getJSONArray("activities").getJSONObject(0).put("vehicleId", 900) },
                { it.getJSONArray("vehicles").getJSONObject(1).put("id", 1) },
                { it.getJSONArray("vehicles").getJSONObject(0).put("purchasePrice", -1) },
                { it.getJSONArray("vehicles").getJSONObject(0).getJSONArray("accessoriesJson").getJSONObject(0).put("price", "bad") },
                { it.getJSONArray("reminders").getJSONObject(0).put("dueTimeSecondOfDay", 86400) },
            )
            mutations.forEach { mutation ->
                val json = JSONObject(BackupJson.encode(sample())); mutation(json)
                try { manager.restore(BackupJson.decode(json.toString())); fail("Invalid file accepted") } catch (_: IllegalArgumentException) { }
                assertEquals(sample().vehicles, manager.snapshot().vehicles)
            }
            try { BackupJson.decode(BackupJson.encode(sample()) + "junk"); fail("Trailing content accepted") } catch (_: IllegalArgumentException) { }
            try { BackupJson.decode("x".repeat(BackupJson.MAX_BYTES + 1)); fail("Oversized file accepted") } catch (_: IllegalArgumentException) { }
        } finally { db.close() }
    }

    @Test fun settingsFailureReportsPartialRestoreWithoutLosingDatabase() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, VehicleMaintenanceDatabase::class.java).build()
        try {
            val preferences = Preferences().apply { fail = true }
            val manager = DataBackupManager(db, preferences, context)
            assertFalse(manager.restore(sample()))
            assertEquals(sample().vehicles, manager.snapshot().vehicles)
            assertEquals(AppSettings(), preferences.state.value)
        } finally { db.close() }
    }
}
