package com.vehiclemaintenancepro.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DataBackupManager @Inject constructor(
    private val database: VehicleMaintenanceDatabase,
    private val settings: SettingsRepository,
    @param:ApplicationContext private val context: Context,
) {
    suspend fun snapshot(): DataBackup = database.withTransaction {
        DataBackup(System.currentTimeMillis(), settings.observeSettings().first(),
            database.vehicleDao().getAllForBackup(), database.activityLogDao().getAllForBackup(),
            database.maintenanceReminderDao().getAllForBackup())
    }

    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val bytes = BackupJson.encode(snapshot()).toByteArray(Charsets.UTF_8)
        require(bytes.size <= BackupJson.MAX_BYTES) { "Data melebihi batas cadangan 10 MB." }
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("File tujuan tidak bisa dibuka.")
        output.use { it.write(bytes) }
    }

    suspend fun inspect(uri: Uri): DataBackup = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri) ?: error("File tidak bisa dibuka.")
        val bytes = input.use { stream ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                require(output.size() + count <= BackupJson.MAX_BYTES) { "File melebihi batas 10 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        require(bytes.size <= BackupJson.MAX_BYTES) { "File melebihi batas 10 MB." }
        BackupJson.decode(bytes.toString(Charsets.UTF_8))
    }

    suspend fun restore(data: DataBackup): Boolean = withContext(Dispatchers.IO) {
        val checked = BackupJson.decode(BackupJson.encode(data))
        database.withTransaction {
            database.activityLogDao().clearForRestore()
            database.maintenanceReminderDao().clearForRestore()
            database.vehicleDao().clearForRestore()
            database.vehicleDao().insertBackup(checked.vehicles)
            database.activityLogDao().insertBackup(checked.activities)
            database.maintenanceReminderDao().insertBackup(checked.reminders)
        }
        try {
            settings.updateSettings(checked.settings)
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        }
    }
}
