package com.vehiclemaintenancepro.core.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vehiclemaintenancepro.MainActivity
import com.vehiclemaintenancepro.R
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.ReminderAlertPolicy
import com.vehiclemaintenancepro.domain.model.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderNotificationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = Room.databaseBuilder(
            applicationContext,
            VehicleMaintenanceDatabase::class.java,
            AppConstants.DATABASE_NAME,
        )
            .addMigrations(
                VehicleMaintenanceDatabase.MIGRATION_1_2,
                VehicleMaintenanceDatabase.MIGRATION_2_3,
            )
            .build()

        try {
            val vehicles = database.vehicleDao()
                .getVehicles()
                .map { it.toDomain() }
                .associateBy { it.id }
            val reminders = database.maintenanceReminderDao()
                .getPendingReminders()
                .map { it.toDomain() }
                .filter { reminder ->
                    ReminderAlertPolicy.shouldAlert(
                        reminder = reminder,
                        vehicle = vehicles[reminder.vehicleId],
                    )
                }

            if (reminders.isNotEmpty()) {
                postReminderNotification(
                    reminders = reminders,
                    vehicles = vehicles,
                )
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            database.close()
        }
    }

    private fun postReminderNotification(
        reminders: List<MaintenanceReminder>,
        vehicles: Map<Long, Vehicle>,
    ) {
        if (!canPostNotifications()) return

        val firstReminder = reminders.first()
        val firstVehicle = vehicles[firstReminder.vehicleId]
        val title = if (reminders.size == 1) {
            firstReminder.title
        } else {
            "${reminders.size} pengingat kendaraan"
        }
        val content = if (reminders.size == 1) {
            listOfNotNull(
                firstVehicle?.displayName(),
                firstReminder.alertLabel(),
            ).joinToString(separator = " - ")
        } else {
            "Ada jadwal servis, oli, pajak, atau STNK yang perlu dicek."
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            ReminderNotificationScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        @Suppress("MissingPermission")
        NotificationManagerCompat.from(applicationContext).notify(
            REMINDER_NOTIFICATION_ID,
            notification,
        )
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun Vehicle.displayName(): String = listOf(brand, model)
        .filter { it.isNotBlank() }
        .joinToString(separator = " ")
        .ifBlank { "Kendaraan" }

    private fun MaintenanceReminder.alertLabel(): String {
        dueDate?.let { date ->
            val daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), date)
            return when {
                daysUntilDue < 0 -> "Lewat ${Formatters.date(date)}"
                daysUntilDue == 0L -> "Hari ini"
                else -> "H-$daysUntilDue (${Formatters.date(date)})"
            }
        }
        dueOdometerKm?.let { return Formatters.odometer(it) }
        return "Perlu dicek"
    }

    private companion object {
        const val REMINDER_NOTIFICATION_ID = 20260810
    }
}
