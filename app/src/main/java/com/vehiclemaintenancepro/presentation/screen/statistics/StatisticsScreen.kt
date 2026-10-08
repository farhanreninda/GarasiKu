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
import androidx.compose.foundation.lazy.items
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
import com.vehiclemaintenancepro.domain.model.ActivityCategory
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
    onOpenVehicles: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(state, viewModel::selectVehicle, { onOpenService(state.selectedVehicleId) }, onAddVehicle, onOpenVehicles)
}

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onSelectVehicle: (Long?) -> Unit = {},
    onOpenService: () -> Unit = {},
    onAddVehicle: () -> Unit = {},
    onOpenVehicles: () -> Unit = {},
) {
    val currentMonth = YearMonth.now()
    val months = remember(currentMonth) { (5L downTo 0L).map { currentMonth.minusMonths(it) } }
    var selectedMonthText by rememberSaveable { mutableStateOf(currentMonth.toString()) }
    val selectedMonth = YearMonth.parse(selectedMonthText)
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
    val monthActivities = remember(state.activities, selectedMonth) {
        state.activities.filter { YearMonth.from(it.occurredAt.atZone(ZoneId.systemDefault())) == selectedMonth }
            .sortedByDescending { it.occurredAt }
    }
    val categoryTotals = remember(monthActivities) {
        listOf("Servis & oli" to monthActivities.filter { it.category == ActivityCategory.Service || it.category == ActivityCategory.OilChange }.sumOf { it.costAmount ?: 0L },
            "Perbaikan" to monthActivities.filter { it.category == ActivityCategory.Repair }.sumOf { it.costAmount ?: 0L },
            "Aksesori" to monthActivities.filter { it.category == ActivityCategory.Accessory }.sumOf { it.costAmount ?: 0L },
            "Lainnya" to monthActivities.filter { it.category == null || it.category == ActivityCategory.Other }.sumOf { it.costAmount ?: 0L })
    }
    val ledgerVehicles = state.vehicles.filter { state.selectedVehicleId == null || it.id == state.selectedVehicleId }
    val purchaseTotal = ledgerVehicles.sumOf { it.purchasePrice ?: 0L }
    val accessoryItems = ledgerVehicles.flatMap { it.accessories }
    val accessoryTotal = accessoryItems.sumOf { it.price ?: 0L } + state.activities.filter { it.category == ActivityCategory.Accessory }.sumOf { it.costAmount ?: 0L }
    val serviceTotal = state.activities.filter { it.category != ActivityCategory.Accessory }.sumOf { it.costAmount ?: 0L }

    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { PageHeader("Biaya kendaraan", "Pembelian, aksesori, dan perawatan dalam bagian terpisah.") }
                state.errorMessage?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                if (state.vehicles.isEmpty() && state.errorMessage == null) {
                    item {
                        PanelCard {
                            Text("Mulai dari kendaraanmu", style = MaterialTheme.typography.titleLarge)
                            Text("Tambahkan kendaraan untuk melihat biaya dan jadwal perawatannya.", modifier = Modifier.padding(vertical = 12.dp))
                            Button(onClick = onAddVehicle, modifier = Modifier.fillMaxWidth()) { Text("Tambah kendaraan") }
                        }
                    }
                } else if (state.errorMessage == null) {
                    item {
                        FilterOptions(listOf<Long?>(null) + state.vehicles.map { it.id }, state.selectedVehicleId,
                            { id -> state.vehicles.firstOrNull { it.id == id }?.licensePlate ?: "Semua" },
                            onSelectVehicle, Modifier.fillMaxWidth())
                    }
                    item {
                        PanelCard {
                            SectionHeader("Buku biaya", actionLabel = "Buka garasi", onAction = onOpenVehicles)
                            Text("Total yang sudah tercatat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.currency(purchaseTotal + accessoryTotal + serviceTotal), style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 12.dp))
                            LedgerCost("Pembelian motor / mobil", purchaseTotal,
                                "${ledgerVehicles.count { it.purchasePrice == null }} kendaraan belum memiliki harga")
                            LedgerCost("Aksesori", accessoryTotal, "${accessoryItems.size} item • ${accessoryItems.count { it.price == null }} harga belum dicatat")
                            LedgerCost("Servis & perawatan", serviceTotal, "Biaya tercatat sepanjang riwayat")
                            Text("Harga motor dan aksesori tanpa tanggal tidak dimasukkan ke grafik biaya servis bulanan.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    item {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Biaya ${selectedMonth.label()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.currency(totals[selectedMonth] ?: 0L), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(vehicleName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Dari aktivitas dengan biaya yang kamu catat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (categoryTotals.any { it.second > 0L }) {
                                    val colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.outline)
                                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                                        categoryTotals.forEachIndexed { index, (_, amount) ->
                                            if (amount > 0L) Box(Modifier.weight(amount.toFloat()).height(10.dp).background(colors[index]))
                                        }
                                    }
                                    BoxWithConstraints {
                                        val columns = if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) 1 else 2
                                        val cellWidth = (maxWidth - 12.dp * (columns - 1)) / columns
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            categoryTotals.forEach { (label, amount) ->
                                                Surface(Modifier.width(cellWidth), shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.surfaceVariant) {
                                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text(label, style = MaterialTheme.typography.labelMedium)
                                                        Text(Formatters.currency(amount), style = MaterialTheme.typography.titleMedium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        FilterOptions(months, selectedMonth, { it.shortLabel() }, { selectedMonthText = it.toString() })
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
                            OutlinedButton(onClick = onOpenService, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Catat biaya di Servis") }
                        }
                    }
                    item { SectionHeader("Pengeluaran terakhir", actionLabel = "Catat", onAction = onOpenService) }
                    if (monthActivities.isEmpty()) {
                        item { PanelCard { Text("Belum ada pengeluaran bulan ini", style = MaterialTheme.typography.titleMedium)
                            Text("Biaya akan tampil setelah kamu mencatat perawatan pada bulan yang dipilih.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)) } }
                    }
                    items(monthActivities.take(5), key = { "expense-${it.id}" }) { activity ->
                        PanelCard {
                            Text(activity.title, style = MaterialTheme.typography.titleMedium)
                            Text(activity.location ?: "Lokasi belum dicatat", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp))
                            Text(Formatters.currency(activity.costAmount ?: 0L), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    item { Button(onClick = onOpenService, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Text("Catat pengeluaran") } }
                    item { SectionHeader("Jadwal perawatan", actionLabel = "Kelola", onAction = onOpenService) }
                    item { Text("$dueCount jatuh tempo • ${state.reminders.size} pengingat belum selesai", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (attention.isEmpty()) {
                        item {
                            PanelCard {
                                Text("Belum ada jadwal perawatan", style = MaterialTheme.typography.titleMedium)
                                Text("Buat pengingat servis, oli, pajak, atau STNK di halaman Servis.", modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedButton(onClick = onOpenService, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Buka Servis") }
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

@Composable
private fun LedgerCost(label: String, amount: Long, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(Formatters.currency(amount), style = MaterialTheme.typography.titleLarge)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun YearMonth.label(): String = format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID")))
private fun YearMonth.shortLabel(): String = format(DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("id-ID")))
