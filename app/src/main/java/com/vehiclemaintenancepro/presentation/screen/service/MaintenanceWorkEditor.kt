package com.vehiclemaintenancepro.presentation.screen.service

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.domain.model.MaintenanceAction
import com.vehiclemaintenancepro.domain.model.MaintenanceComponent
import com.vehiclemaintenancepro.domain.model.MaintenanceWorkItem
import com.vehiclemaintenancepro.presentation.component.SelectionField

@Composable
internal fun MaintenanceWorkEditor(items: List<MaintenanceWorkItem>, enabled: Boolean, onChange: (List<MaintenanceWorkItem>) -> Unit) {
    var componentName by rememberSaveable { mutableStateOf(MaintenanceComponent.EngineOil.name) }
    var actionName by rememberSaveable { mutableStateOf(MaintenanceAction.Replace.name) }
    var detail by rememberSaveable { mutableStateOf("") }
    var intervalKm by rememberSaveable { mutableStateOf("") }
    var intervalMonths by rememberSaveable { mutableStateOf("") }
    var showIntervals by rememberSaveable { mutableStateOf(false) }
    val component = MaintenanceComponent.valueOf(componentName)
    val action = MaintenanceAction.valueOf(actionName)
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        Text("Pekerjaan / komponen", style = MaterialTheme.typography.titleMedium)
        Text("Pisahkan tiap komponen agar jadwal berikutnya dapat dihitung.", style = MaterialTheme.typography.bodyMedium)
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${item.component.label} • ${item.action.label}")
                    if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onChange(items - item) }, enabled = enabled) { Text("Hapus") }
            }
        }
        SelectionField(component.label, MaintenanceComponent.entries.map { it.label }, "Komponen", enabled) { selected ->
            componentName = MaintenanceComponent.entries.first { it.label == selected }.name
        }
        ChoiceRow(MaintenanceAction.entries, action, { it.label }) { actionName = it.name }
        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Merek / rincian komponen") },
            modifier = Modifier.fillMaxWidth(), enabled = enabled)
        TextButton(onClick = { showIntervals = !showIntervals }, enabled = enabled) { Text(if (showIntervals) "Tutup interval khusus" else "Atur interval khusus (opsional)") }
        if (showIntervals) {
        Text("Kosongkan untuk memakai acuan kendaraan yang tersedia.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it.filter(Char::isDigit) },
            label = { Text("Ulangi setiap berapa km") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = enabled)
        OutlinedTextField(value = intervalMonths, onValueChange = { intervalMonths = it.filter(Char::isDigit) },
            label = { Text("Ulangi setiap berapa bulan") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = enabled)
        }
        val valid = (intervalKm.isBlank() || intervalKm.toLongOrNull()?.let { it > 0 } == true) &&
            (intervalMonths.isBlank() || intervalMonths.toIntOrNull()?.let { it in 1..1200 } == true)
        if (!valid) Text("Interval harus positif; maksimal 1.200 bulan.", color = MaterialTheme.colorScheme.error)
        OutlinedButton(enabled = enabled && valid, onClick = {
            val item = MaintenanceWorkItem(component, action, detail.trim(), intervalKm.toLongOrNull(), intervalMonths.toIntOrNull())
            onChange(items.filterNot { it.component == component } + item)
            detail = ""
            intervalKm = ""
            intervalMonths = ""
        }) { Text(if (items.any { it.component == component }) "Perbarui komponen" else "Tambahkan komponen") }
    }
}
