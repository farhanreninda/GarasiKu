package com.vehiclemaintenancepro.presentation.screen.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.core.common.SaveState
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.domain.model.ActivityLogCreateRequest
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ServiceViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    private val notice = MutableStateFlow<String?>(null)
    private val saving = MutableStateFlow(SaveState())
    val saveState = saving.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = vehicleRepository.observeVehicles().flatMapLatest { vehicles ->
        val activeVehicle = vehicles.firstOrNull { it.isActive } ?: vehicles.firstOrNull()
        val reminders = activeVehicle?.let { maintenanceRepository.observePendingRemindersForVehicle(it.id) } ?: flowOf(emptyList())
        val activities = activeVehicle?.let { maintenanceRepository.observeRecentActivityForVehicle(it.id, ACTIVITY_LIMIT) } ?: flowOf(emptyList())
        combine(reminders, activities, notice) { pending, recent, noticeMessage ->
            ServiceUiState(
                isLoading = false,
                vehicles = vehicles,
                activeVehicle = activeVehicle,
                reminders = pending,
                activities = recent,
                notice = noticeMessage,
            )
        }
    }
        .catch { throwable ->
            emit(
                ServiceUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Data servis belum bisa dimuat",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ServiceUiState(),
        )

    fun addReminder(request: MaintenanceReminderCreateRequest, onSuccess: () -> Unit = {}) {
        runOperation(successMessage = "Reminder berhasil dibuat", onSuccess = onSuccess) {
            maintenanceRepository.addReminder(request)
        }
    }

    fun completeReminder(reminder: MaintenanceReminder) {
        runOperation(successMessage = "Reminder ditandai selesai") {
            maintenanceRepository.completeReminder(reminder)
        }
    }

    fun addActivity(request: ActivityLogCreateRequest, onSuccess: () -> Unit = {}) {
        runOperation(successMessage = "Aktivitas berhasil dicatat", onSuccess = onSuccess) {
            maintenanceRepository.addActivity(request)
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
                ReminderNotificationScheduler.checkNow(context)
                onSuccess()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = "Data belum tersimpan. Coba lagi."
                saving.value = SaveState(errorMessage = message)
                notice.value = message
            } finally {
                saving.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val ACTIVITY_LIMIT = Int.MAX_VALUE
    }
}
