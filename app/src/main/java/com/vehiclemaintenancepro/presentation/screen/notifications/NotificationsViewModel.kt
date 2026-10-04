package com.vehiclemaintenancepro.presentation.screen.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.ReminderAlertPolicy
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    maintenanceRepository: MaintenanceRepository,
) : ViewModel() {
    val uiState = combine(
        vehicleRepository.observeVehicles(),
        maintenanceRepository.observePendingReminders(),
    ) { vehicles, reminders ->
        val activeVehicle = vehicles.firstOrNull { it.isActive } ?: vehicles.firstOrNull()
        val activeReminders = activeVehicle?.let { vehicle ->
            reminders
                .filter { it.vehicleId == vehicle.id }
                .filter { ReminderAlertPolicy.shouldAlert(it, vehicle) }
                .sortedWith(reminderComparator)
        }.orEmpty()

        NotificationsUiState(
            isLoading = false,
            activeVehicle = activeVehicle,
            reminders = activeReminders,
        )
    }
        .catch { throwable ->
            emit(
                NotificationsUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Notifikasi belum bisa dimuat",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = NotificationsUiState(),
        )

    private companion object {
        val reminderComparator = compareBy<MaintenanceReminder> { it.dueDate ?: java.time.LocalDate.MAX }
            .thenBy { it.dueOdometerKm ?: Long.MAX_VALUE }
    }
}
