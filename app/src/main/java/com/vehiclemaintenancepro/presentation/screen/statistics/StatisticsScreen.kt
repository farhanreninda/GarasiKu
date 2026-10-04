package com.vehiclemaintenancepro.presentation.screen.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.presentation.component.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onOpenService: (Long?) -> Unit = {},
    onAddVehicle: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(state, viewModel::selectVehicle, { onOpenService(state.selectedVehicleId) }, onAddVehicle)
}

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onSelectVehicle: (Long?) -> Unit = {},
    onOpenService: () -> Unit = {},
    onAddVehicle: () -> Unit = {},
) {
    val currentMonth = YearMonth.now()
    val months = remember(currentMonth) { (5L downTo 0L).map { currentMonth.minusMonths(it) } }
    var selectedMonthText by rememberSaveable { mutableStateOf(currentMonth.toString()) }
    val selectedMonth = YearMonth.parse(selectedMonthText)
    val monthScrollState = rememberLazyListState(initialFirstVisibleItemIndex = months.indexOf(selectedMonth).coerceAtLeast(0))
    val totals = remember(state.activities, months) {
        val grouped = state.activities.groupBy { YearMonth.from(it.occurredAt.atZone(ZoneId.systemDefault())) }
        months.associateWith { month -> grouped[month].orEmpty().sumOf { it.costAmount ?: 0L } }
    }
    val today = LocalDate.now()
    val attention = remember(state.reminders, state.vehicles, today) {
        state.reminders.sortedWith(compareBy<MaintenanceReminder> {
            if (it.isDue(state.vehicles, today)) 0 else 1
        }.thenBy { it.dueDate ?: LocalDate.MAX }.thenBy { it.dueOdometerKm ?: Long.MAX_VALUE })
    }
    val dueCount = attention.count { it.isDue(state.vehicles, today) }
    val vehicleName = state.vehicles.firstOrNull { it.id == state.selectedVehicleId }?.let { "${it.brand} ${it.model}" } ?: "Semua kendaraan"

    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                item { PageHeader("Biaya kendaraan", "Lihat pengeluaran yang sudah kamu catat.") }
                state.errorMessage?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                if (state.vehicles.isEmpty() && state.errorMessage == null) {
                    item {
                        PanelCard {
                            Text("Mulai dari kendaraanmu", style = MaterialTheme.typography.titleLarge)
                            Text("Tambahkan kendaraan untuk melihat biaya dan jadwal perawatannya.", modifier = Modifier.padding(vertical = 12.dp))
                            Button(onClick = onAddVehicle) { Text("Tambah kendaraan") }
                        }
                    }
                } else if (state.errorMessage == null) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(state.selectedVehicleId == null, { onSelectVehicle(null) }, { Text("Semua") }) }
                            items(state.vehicles, key = { it.id }) { vehicle ->
                                FilterChip(state.selectedVehicleId == vehicle.id, { onSelectVehicle(vehicle.id) }, { Text(vehicle.licensePlate) })
                            }
                        }
                    }
                    item {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Biaya ${selectedMonth.label()}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(Formatters.currency(totals[selectedMonth] ?: 0L), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(vehicleName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Dari aktivitas dengan biaya yang kamu catat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    item {
                        LazyRow(state = monthScrollState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(months, key = { it.toString() }) { month ->
                                FilterChip(selectedMonth == month, { selectedMonthText = month.toString() }, { Text(month.shortLabel()) })
                            }
                        }
                    }
                    item {
                        PanelCard {
                            Text("Tren 6 bulan", style = MaterialTheme.typography.titleMedium)
                            Text("Ketuk bulan untuk melihat biayanya.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            val maximum = (totals.values.maxOrNull() ?: 0L).coerceAtLeast(1L)
                            if (totals.values.any { it > 0L }) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            months.forEach { month ->
                                val amount = totals[month] ?: 0L
                                Column(Modifier.weight(1f).selectable(selected = month == selectedMonth, role = Role.RadioButton,
                                    onClick = { selectedMonthText = month.toString() }).semantics { contentDescription = "${month.label()}, ${Formatters.currency(amount)}" }.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small), contentAlignment = Alignment.BottomCenter) {
                                        if (amount > 0L) Box(Modifier.fillMaxWidth().height((120 * (amount.toDouble() / maximum).toFloat()).dp.coerceAtLeast(2.dp))
                                            .background(if (month == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.shapes.small))
                                    }
                                    Text(month.format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("id-ID"))), style = MaterialTheme.typography.labelSmall,
                                        color = if (month == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                            }
                            }
                            Text("Total: ${Formatters.currency(totals.values.sum())}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
                            if (totals.values.all { it == 0L }) Text("Belum ada biaya tercatat pada periode ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = onOpenService) { Text("Catat biaya di Servis") }
                        }
                    }
                    item { SectionHeader("Jadwal perawatan", actionLabel = "Kelola", onAction = onOpenService) }
                    item { Text("$dueCount jatuh tempo • ${state.reminders.size} pengingat belum selesai", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (attention.isEmpty()) {
                        item {
                            PanelCard {
                                Text("Belum ada jadwal perawatan", style = MaterialTheme.typography.titleMedium)
                                Text("Buat pengingat servis, oli, pajak, atau STNK di halaman Servis.", modifier = Modifier.padding(vertical = 8.dp))
                                TextButton(onClick = onOpenService) { Text("Buka Servis") }
                            }
                        }
                    }
                    items(attention.take(5), key = { it.id }) { reminder ->
                        PanelCard(modifier = Modifier.clickable(onClick = onOpenService, onClickLabel = "Kelola pengingat")) {
                            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                            Text(state.vehicles.firstOrNull { it.id == reminder.vehicleId }?.licensePlate.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(listOfNotNull(reminder.dueDate?.let(Formatters::date), reminder.dueOdometerKm?.let(Formatters::odometer)).joinToString(" • "), modifier = Modifier.padding(top = 8.dp))
                            if (reminder.isDue(state.vehicles, today)) Text("Sudah jatuh tempo", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

internal fun MaintenanceReminder.isDue(vehicles: List<Vehicle>, today: LocalDate): Boolean =
    dueDate?.let { !it.isAfter(today) } == true ||
        dueOdometerKm?.let { due -> vehicles.any { it.id == vehicleId && it.odometerKm >= due } } == true

private fun YearMonth.label(): String = format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID")))
private fun YearMonth.shortLabel(): String = format(DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("id-ID")))
