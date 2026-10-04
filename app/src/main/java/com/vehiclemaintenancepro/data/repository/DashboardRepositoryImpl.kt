package com.vehiclemaintenancepro.data.repository

import com.vehiclemaintenancepro.core.common.ResultState
import com.vehiclemaintenancepro.data.local.dao.ActivityLogDao
import com.vehiclemaintenancepro.data.local.dao.MaintenanceReminderDao
import com.vehiclemaintenancepro.data.local.dao.VehicleDao
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.domain.model.DashboardSummary
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.ReminderAlertPolicy
import com.vehiclemaintenancepro.domain.model.ReminderType
import com.vehiclemaintenancepro.domain.repository.DashboardRepository
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val reminderDao: MaintenanceReminderDao,
    private val activityLogDao: ActivityLogDao,
    private val settingsRepository: SettingsRepository,
) : DashboardRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDashboardSummary(): Flow<ResultState<DashboardSummary>> {
        val vehiclesFlow = vehicleDao.observeVehicles().map { vehicles ->
            vehicles.map { it.toDomain() }
        }
        val settingsFlow = settingsRepository.observeSettings()

        val summaryFlow: Flow<ResultState<DashboardSummary>> = vehiclesFlow.flatMapLatest { vehicles ->
            val selectedVehicle = vehicles.firstOrNull { it.isActive } ?: vehicles.firstOrNull()
            val remindersFlow = selectedVehicle?.let { vehicle ->
                reminderDao.observePendingRemindersForVehicle(vehicle.id).map { reminders ->
                    reminders.map { it.toDomain() }
                }
            } ?: flowOf(emptyList())
            val monthlyExpenseFlow = selectedVehicle?.let { vehicle ->
                activityLogDao.observeExpenseTotalBetweenForVehicle(
                    vehicleId = vehicle.id,
                    startMillis = currentMonthStartMillis(),
                    endMillis = nextMonthStartMillis(),
                )
            } ?: flowOf(0L)

            combine(
                remindersFlow,
                monthlyExpenseFlow,
                settingsFlow,
            ) { reminders, monthlyExpense, settings ->
                ResultState.Success<DashboardSummary>(
                    DashboardSummary(
                        userName = settings.userName,
                        activeVehicle = selectedVehicle,
                        vehicles = vehicles,
                        nextService = reminders.firstByType(ReminderType.Service),
                        nextOilChange = reminders.firstByType(ReminderType.OilChange),
                        taxReminder = reminders.firstByType(ReminderType.Tax),
                        registrationReminder = reminders.firstByType(ReminderType.Registration),
                        insuranceReminder = reminders.firstByType(ReminderType.Insurance),
                        monthlyExpense = monthlyExpense,
                        averageFuelConsumptionKmPerLiter = null,
                        nearestReminder = reminders.nearest(),
                        alertReminderCount = reminders.count { reminder ->
                            ReminderAlertPolicy.shouldAlert(reminder, selectedVehicle)
                        },
                    ),
                )
            }
        }

        return summaryFlow
            .onStart { emit(ResultState.Loading) }
            .catch { throwable ->
                emit(
                    ResultState.Error(
                        message = throwable.message.orEmpty(),
                        throwable = throwable,
                    ),
                )
            }
    }

    private fun List<MaintenanceReminder>.firstByType(type: ReminderType): MaintenanceReminder? =
        firstOrNull { it.type == type }

    private fun List<MaintenanceReminder>.nearest(): MaintenanceReminder? = minWithOrNull(
        compareBy<MaintenanceReminder> { it.dueDate ?: LocalDate.MAX }
            .thenBy { it.dueOdometerKm ?: Long.MAX_VALUE },
    )

    private fun currentMonthStartMillis(): Long {
        val zoneId = ZoneId.systemDefault()
        return LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun nextMonthStartMillis(): Long {
        val zoneId = ZoneId.systemDefault()
        return LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

}

