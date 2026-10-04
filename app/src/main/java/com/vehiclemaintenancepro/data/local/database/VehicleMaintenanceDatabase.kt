package com.vehiclemaintenancepro.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vehiclemaintenancepro.data.local.dao.ActivityLogDao
import com.vehiclemaintenancepro.data.local.dao.MaintenanceReminderDao
import com.vehiclemaintenancepro.data.local.dao.VehicleDao
import com.vehiclemaintenancepro.data.local.entity.ActivityLogEntity
import com.vehiclemaintenancepro.data.local.entity.MaintenanceReminderEntity
import com.vehiclemaintenancepro.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        MaintenanceReminderEntity::class,
        ActivityLogEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class VehicleMaintenanceDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun maintenanceReminderDao(): MaintenanceReminderDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activity_logs ADD COLUMN category TEXT")
                db.execSQL("ALTER TABLE activity_logs ADD COLUMN odometer_km INTEGER")
                db.execSQL("ALTER TABLE activity_logs ADD COLUMN location TEXT")
                db.execSQL("ALTER TABLE activity_logs ADD COLUMN work_items_json TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activity_logs ADD COLUMN cost_amount INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN vehicle_type TEXT NOT NULL DEFAULT 'Car'")
            }
        }
    }
}
