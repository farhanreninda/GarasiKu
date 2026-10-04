package com.vehiclemaintenancepro.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderNotificationScheduler {
    const val CHANNEL_ID = "vehicle_reminders"

    private const val CHANNEL_NAME = "Pengingat kendaraan"
    private const val WORK_NAME = "daily_vehicle_reminder_notifications"

    fun initialize(context: Context) {
        ensureNotificationChannel(context)
        scheduleDailyReminderCheck(context)
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifikasi servis, oli, pajak, dan STNK kendaraan."
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleDailyReminderCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}
