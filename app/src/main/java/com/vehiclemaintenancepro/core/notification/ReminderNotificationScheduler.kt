package com.vehiclemaintenancepro.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit

object ReminderNotificationScheduler {
    const val CHANNEL_ID = "vehicle_reminders"

    const val TEST_NOTIFICATION_ID = 20261004

    fun blockedReason(context: Context, enabled: Boolean): String? {
        if (!enabled) return "Pengingat dimatikan di Profil."
        if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return "Izin notifikasi Android belum diberikan."
        }
        if (!androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) return "Notifikasi aplikasi diblokir di Android."
        if (context.getSystemService<NotificationManager>()?.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) {
            return "Kanal pengingat dinonaktifkan di Android."
        }
        return null
    }

    @Suppress("MissingPermission")
    fun sendTest(context: Context, enabled: Boolean): String {
        ensureNotificationChannel(context)
        blockedReason(context, enabled)?.let { return it }
        val intent = android.app.PendingIntent.getActivity(context, TEST_NOTIFICATION_ID,
            android.content.Intent(context, com.vehiclemaintenancepro.MainActivity::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        return try {
            androidx.core.app.NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID,
                androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(com.vehiclemaintenancepro.R.drawable.ic_launcher_foreground)
                    .setContentTitle("Tes notifikasi GarasiKu")
                    .setContentText("Pengingat siap. Ini notifikasi percobaan, bukan jadwal perawatan.")
                    .setContentIntent(intent).setAutoCancel(true).build())
            "Tes dikirim. Periksa panel notifikasi HP; suara mengikuti setelan Android."
        } catch (_: SecurityException) {
            "Tes belum terkirim. Periksa izin notifikasi Android."
        }
    }

    private const val CHANNEL_NAME = "Pengingat kendaraan"
    private const val WORK_NAME = "daily_vehicle_reminder_notifications"

    fun initialize(context: Context) {
        ensureNotificationChannel(context)
        scheduleDailyReminderCheck(context)
    }

    fun checkNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "check_vehicle_reminders_now", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ReminderNotificationWorker>().build(),
        )
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
