package com.vehiclemaintenancepro.di

import android.content.Context
import androidx.room.Room
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.data.local.dao.ActivityLogDao
import com.vehiclemaintenancepro.data.local.dao.MaintenanceReminderDao
import com.vehiclemaintenancepro.data.local.dao.VehicleDao
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): VehicleMaintenanceDatabase = Room.databaseBuilder(
        context,
        VehicleMaintenanceDatabase::class.java,
        AppConstants.DATABASE_NAME,
    )
        .addMigrations(
            VehicleMaintenanceDatabase.MIGRATION_1_2,
            VehicleMaintenanceDatabase.MIGRATION_2_3,
            VehicleMaintenanceDatabase.MIGRATION_3_4,
            VehicleMaintenanceDatabase.MIGRATION_4_5,
        )
        .build()

    @Provides
    fun provideVehicleDao(database: VehicleMaintenanceDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideMaintenanceReminderDao(database: VehicleMaintenanceDatabase): MaintenanceReminderDao =
        database.maintenanceReminderDao()

    @Provides
    fun provideActivityLogDao(database: VehicleMaintenanceDatabase): ActivityLogDao = database.activityLogDao()
}
