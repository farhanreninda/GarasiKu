package com.vehiclemaintenancepro.presentation.screen.settings

import android.net.Uri
import com.vehiclemaintenancepro.data.backup.DataBackup
import com.vehiclemaintenancepro.data.backup.DataBackupManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.core.common.SaveState
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val vehicleRepository: VehicleRepository,
    maintenanceRepository: MaintenanceRepository,
    private val backupManager: DataBackupManager,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    private val preparedBackup = MutableStateFlow<DataBackup?>(null)
    val pendingImport = preparedBackup.asStateFlow()

    fun exportData(uri: Uri) {
        save("Cadangan berhasil diekspor.") { backupManager.export(uri) }
    }

    fun inspectImport(uri: Uri) {
        var data: DataBackup? = null
        save(null, onSuccess = { preparedBackup.value = data }) { data = backupManager.inspect(uri) }
    }

    fun cancelImport() {
        if (!saving.value.isSaving) { preparedBackup.value = null; clearNotice() }
    }

    fun confirmImport() {
        val data = preparedBackup.value ?: return
        var settingsRestored = true
        save(null, onSuccess = {
            preparedBackup.value = null
            notice.value = if (settingsRestored) "Data dan pengaturan berhasil diimpor." else "Data berhasil diimpor, tetapi pengaturan belum dipulihkan. Atur kembali nama dan tema."
        }) { settingsRestored = backupManager.restore(data) }
    }

    private val notice = MutableStateFlow<String?>(null)
    private val saving = MutableStateFlow(SaveState())
    val saveState = saving.asStateFlow()

    private val profile = combine(
        settingsRepository.observeSettings(),
        vehicleRepository.observeVehicles(),
        maintenanceRepository.observePendingReminders(),
    ) { settings, vehicles, reminders ->
        val ids = vehicles.map { it.id }.toSet()
        SettingsUiState(
            isLoading = false,
            userName = settings.userName,
            themeMode = settings.themeMode,
            vehicles = vehicles,
            notificationsEnabled = settings.notificationsEnabled,
            taxReminderDays = settings.taxReminderDays,
            serviceReminderDays = settings.serviceReminderDays,
            pendingReminderCount = reminders.count { it.vehicleId in ids },
        )
    }.catch { emit(SettingsUiState(isLoading = false, errorMessage = "Profil belum bisa dimuat. Buka kembali aplikasi untuk mencoba lagi.")) }

    val uiState = combine(profile, notice, saving) { state, message, save ->
        state.copy(notice = message, isSaving = save.isSaving)
    }.stateIn(viewModelScope, SharingStarted.Lazily, SettingsUiState())

    fun updateUserName(userName: String) {
        save("Nama pengguna disimpan") {
            require(userName.trim().isNotEmpty()) { "Nama pengguna wajib diisi." }
            settingsRepository.updateUserName(userName)
        }
    }

    fun updateOdometer(vehicleId: Long, odometerKm: Long, onSuccess: () -> Unit) {
        save("Odometer diperbarui", { ReminderNotificationScheduler.checkNow(context); onSuccess() }) { vehicleRepository.updateOdometer(vehicleId, odometerKm) }
    }

    fun updateThemeMode(mode: com.vehiclemaintenancepro.domain.model.ThemeMode) {
        save(null) { settingsRepository.updateThemeMode(mode) }
    }

    fun updateNotifications(enabled: Boolean? = null, taxDays: Int? = null, serviceDays: Int? = null) {
        save(null) { settingsRepository.updateNotificationSettings(enabled, taxDays, serviceDays) }
    }

    fun clearNotice() {
        notice.value = null
        if (!saving.value.isSaving) saving.value = SaveState()
    }

    private fun save(message: String?, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        if (saving.value.isSaving) return
        saving.value = SaveState(isSaving = true)
        notice.value = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { block() }
                saving.value = SaveState()
                notice.value = message
                onSuccess()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val failure = if (error is IllegalArgumentException || error is IllegalStateException) error.message ?: "Periksa kembali datanya." else "Operasi gagal. Periksa file dan izin penyimpanannya, lalu coba lagi."
                saving.value = SaveState(errorMessage = failure)
                notice.value = failure
            } finally {
                saving.update { it.copy(isSaving = false) }
            }
        }
    }
}
