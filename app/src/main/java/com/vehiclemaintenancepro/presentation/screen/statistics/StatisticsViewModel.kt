package com.vehiclemaintenancepro.presentation.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    maintenanceRepository: MaintenanceRepository,
) : ViewModel() {
    private val selectedVehicleId = MutableStateFlow<Long?>(null)
    private val month = YearMonth.now()
    private val zone = ZoneId.systemDefault()

    val uiState = combine(
        vehicleRepository.observeVehicles(),
        maintenanceRepository.observePendingReminders(),
        maintenanceRepository.observeExpensesBetween(
            month.minusMonths(5).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli(),
        ),
        selectedVehicleId,
    ) { vehicles, reminders, expenses, selection ->
        val selected = selection?.takeIf { id -> vehicles.any { it.id == id } }
        val ids = vehicles.filter { selected == null || it.id == selected }.map { it.id }.toSet()
        StatisticsUiState(
            isLoading = false,
            vehicles = vehicles,
            selectedVehicleId = selected,
            reminders = reminders.filter { it.vehicleId in ids },
            activities = expenses.filter { it.vehicleId in ids },
        )
    }
        .flowOn(Dispatchers.Default)
        .catch { emit(StatisticsUiState(isLoading = false, errorMessage = "Statistik belum bisa dimuat. Buka kembali aplikasi untuk mencoba lagi.")) }
        .stateIn(viewModelScope, SharingStarted.Lazily, StatisticsUiState())

    fun selectVehicle(id: Long?) {
        selectedVehicleId.value = id
    }
}
