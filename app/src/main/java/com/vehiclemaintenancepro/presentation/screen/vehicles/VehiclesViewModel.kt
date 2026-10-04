package com.vehiclemaintenancepro.presentation.screen.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.core.common.SaveState
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val notice = MutableStateFlow<String?>(null)
    private val saving = MutableStateFlow(SaveState())
    val saveState = saving.asStateFlow()

    private val vehiclesState = vehicleRepository.observeVehicles()
        .map { vehicles ->
            VehiclesUiState(
                isLoading = false,
                vehicles = vehicles,
            )
        }
        .catch { throwable ->
            emit(
                VehiclesUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Data kendaraan belum bisa dimuat",
                ),
            )
        }

    val uiState = combine(vehiclesState, notice) { state, noticeMessage ->
        state.copy(notice = noticeMessage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = VehiclesUiState(),
    )

    fun addVehicle(request: VehicleCreateRequest, onSuccess: () -> Unit = {}) {
        runOperation(
            successMessage = "Kendaraan berhasil disimpan",
            onSuccess = onSuccess,
        ) {
            vehicleRepository.addVehicle(request)
        }
    }

    fun setActiveVehicle(vehicleId: Long, onSuccess: () -> Unit = {}) {
        runOperation(
            successMessage = "Kendaraan tampilan diperbarui",
            onSuccess = onSuccess,
        ) {
            vehicleRepository.setActiveVehicle(vehicleId)
        }
    }

    fun archiveVehicle(vehicleId: Long) {
        runOperation(
            successMessage = "Kendaraan diarsipkan",
        ) {
            vehicleRepository.archiveVehicle(vehicleId)
        }
    }

    fun clearNotice() {
        notice.update { null }
        if (!saving.value.isSaving) saving.value = SaveState()
    }

    private fun runOperation(
        successMessage: String,
        onSuccess: () -> Unit = {},
        block: suspend () -> Unit,
    ) {
        if (saving.value.isSaving) return
        saving.value = SaveState(isSaving = true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    block()
                }
                saving.value = SaveState()
                notice.value = successMessage
                onSuccess()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = if (error is android.database.sqlite.SQLiteConstraintException) {
                    "Nomor polisi sudah terdaftar. Gunakan nomor yang berbeda."
                } else {
                    "Data belum tersimpan. Coba lagi."
                }
                saving.value = SaveState(errorMessage = message)
                notice.value = message
            } finally {
                saving.update { it.copy(isSaving = false) }
            }
        }
    }
}
