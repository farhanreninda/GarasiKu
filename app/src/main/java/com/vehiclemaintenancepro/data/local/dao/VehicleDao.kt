package com.vehiclemaintenancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vehiclemaintenancepro.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY id")
    suspend fun getAllForBackup(): List<VehicleEntity>

    @Query("DELETE FROM vehicles")
    suspend fun clearForRestore()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBackup(rows: List<VehicleEntity>)

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId AND is_archived = 0")
    suspend fun getVehicle(vehicleId: Long): VehicleEntity?

    @Query("UPDATE vehicles SET engine_number = :engineNumber, frame_number = :frameNumber, otr_price = :otrPrice, purchase_price = :purchasePrice, dealer = :dealer, order_date_epoch_day = :orderDate, purchase_date_epoch_day = :deliveryDate, updated_at_millis = :now WHERE id = :vehicleId AND is_archived = 0")
    suspend fun updateDetails(vehicleId: Long, engineNumber: String?, frameNumber: String?, otrPrice: Long?, purchasePrice: Long?, dealer: String?, orderDate: Long?, deliveryDate: Long?, now: Long): Int

    @Query("UPDATE vehicles SET accessories_json = :value, updated_at_millis = :now WHERE id = :vehicleId AND is_archived = 0")
    suspend fun updateAccessories(vehicleId: Long, value: String, now: Long): Int

    @Query("UPDATE vehicles SET odometer_km = :odometerKm, updated_at_millis = :updatedAtMillis WHERE id = :vehicleId AND is_archived = 0 AND odometer_km <= :odometerKm")
    suspend fun updateOdometer(vehicleId: Long, odometerKm: Long, updatedAtMillis: Long): Int

    @Query("SELECT * FROM vehicles WHERE is_archived = 0 ORDER BY is_active DESC, updated_at_millis DESC")
    fun observeVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE is_active = 1 AND is_archived = 0 LIMIT 1")
    fun observeActiveVehicle(): Flow<VehicleEntity?>

    @Query("SELECT COUNT(*) FROM vehicles WHERE is_archived = 0")
    suspend fun countVehicles(): Int

    @Query("SELECT id FROM vehicles WHERE is_active = 1 AND is_archived = 0 LIMIT 1")
    suspend fun getActiveVehicleId(): Long?

    @Query("SELECT id FROM vehicles WHERE is_archived = 0 ORDER BY updated_at_millis DESC LIMIT 1")
    suspend fun getLatestVehicleId(): Long?

    @Query("SELECT * FROM vehicles WHERE is_archived = 0")
    suspend fun getVehicles(): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Query("UPDATE vehicles SET is_active = 0, updated_at_millis = :updatedAtMillis WHERE is_active = 1")
    suspend fun clearActiveVehicles(updatedAtMillis: Long)

    @Query("UPDATE vehicles SET is_active = 1, updated_at_millis = :updatedAtMillis WHERE id = :vehicleId AND is_archived = 0")
    suspend fun markVehicleActive(vehicleId: Long, updatedAtMillis: Long)

    @Query("UPDATE vehicles SET is_archived = 1, is_active = 0, updated_at_millis = :updatedAtMillis WHERE id = :vehicleId")
    suspend fun archiveVehicle(vehicleId: Long, updatedAtMillis: Long)
}
