package com.vehiclemaintenancepro

import android.app.Application
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VehicleMaintenanceProApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderNotificationScheduler.initialize(this)
    }
}
