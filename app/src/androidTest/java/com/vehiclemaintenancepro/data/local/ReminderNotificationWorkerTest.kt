package com.vehiclemaintenancepro.data.local

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.core.notification.ReminderNotificationWorker
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.local.entity.*
import com.vehiclemaintenancepro.data.repository.SettingsRepositoryImpl
import com.vehiclemaintenancepro.domain.model.AppSettings
import java.io.File
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.Executor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class ReminderNotificationWorkerTest {
    @Test fun workerPostsTaxAndServiceAtConfiguredLeadAndCancelsWhenDisabled() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "notification-test-${UUID.randomUUID()}").apply { mkdirs() }
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getDatabasePath(name: String): File = File(directory, name)
        }
        val repository = SettingsRepositoryImpl(base)
        val original = repository.observeSettings().first()
        val manager = base.getSystemService(NotificationManager::class.java)
        val database = Room.databaseBuilder(isolated, VehicleMaintenanceDatabase::class.java, AppConstants.DATABASE_NAME).build()
        val executor = Executor { it.run() }
        fun worker() = ReminderNotificationWorker(isolated, WorkerParameters(UUID.randomUUID(), Data.EMPTY, emptyList(),
            WorkerParameters.RuntimeExtras(), 0, 0, executor, Dispatchers.IO, WorkManagerTaskExecutor(executor),
            object : WorkerFactory() { override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? = null },
            ProgressUpdater { _, _, _ -> error("Unused progress") }, ForegroundUpdater { _, _, _ -> error("Unused foreground") }))
        suspend fun settings(value: AppSettings) {
            repository.updateSettings(value)
            withTimeout(20000) {
                WorkManager.getInstance(base).getWorkInfosForUniqueWorkFlow("check_vehicle_reminders_now")
                    .first { infos -> infos.isNotEmpty() && infos.all { it.state.isFinished } }
            }
        }
        fun posted() = manager.activeNotifications.firstOrNull { it.id == 20260810 }?.notification
        try {
            database.vehicleDao().insertVehicle(VehicleEntity(1, "Motorcycle", null, "Honda", "QC Motor", 2023, "QC NOTIF", null, null, null,
                "Cvt", "Gasoline", 10000, null, null, null, true, false, 1, 1))
            val today = LocalDate.now()
            database.maintenanceReminderDao().insertReminder(MaintenanceReminderEntity(1, 1, "Tax", "QC Pajak", today.plusDays(14).toEpochDay(), null, null, false, 1, 1))
            database.maintenanceReminderDao().insertReminder(MaintenanceReminderEntity(2, 1, "Service", "QC Servis", today.plusDays(4).toEpochDay(), null, null, false, 1, 1))
            settings(original.copy(notificationsEnabled = true, taxReminderDays = 7, serviceReminderDays = 3))
            assertEquals(ListenableWorker.Result.success(), worker().doWork()); assertNull(posted())
            settings(original.copy(notificationsEnabled = true, taxReminderDays = 14, serviceReminderDays = 3))
            assertEquals(ListenableWorker.Result.success(), worker().doWork())
            assertEquals("QC Pajak", posted()!!.extras.getString(Notification.EXTRA_TITLE))
            assertTrue(posted()!!.extras.getString(Notification.EXTRA_TEXT)!!.contains("H-14"))
            settings(original.copy(notificationsEnabled = true, taxReminderDays = 14, serviceReminderDays = 4))
            assertEquals(ListenableWorker.Result.success(), worker().doWork())
            val text = posted()!!.extras.getString(Notification.EXTRA_BIG_TEXT)!!
            assertTrue(text.contains("QC Pajak") && text.contains("QC Servis") && text.contains("QC NOTIF"))
            settings(original.copy(notificationsEnabled = false))
            assertEquals(ListenableWorker.Result.success(), worker().doWork()); assertNull(posted())
        } finally {
            database.close()
            repository.updateSettings(original)
            manager.cancel(20260810)
            directory.deleteRecursively()
        }
    }
}
