package com.vehiclemaintenancepro.presentation.screen.settings

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
) : ViewModel() {
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
            vehicles = vehicles,
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
        save("Odometer diperbarui", onSuccess) { vehicleRepository.updateOdometer(vehicleId, odometerKm) }
    }

    fun clearNotice() {
        notice.value = null
        if (!saving.value.isSaving) saving.value = SaveState()
    }

    private fun save(message: String, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
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
                val failure = if (error is IllegalArgumentException || error is IllegalStateException) error.message ?: "Periksa kembali datanya." else "Data belum tersimpan. Coba lagi."
                saving.value = SaveState(errorMessage = failure)
                notice.value = failure
            } finally {
                saving.update { it.copy(isSaving = false) }
            }
        }
    }
}
