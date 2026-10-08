package com.vehiclemaintenancepro.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vehiclemaintenancepro.core.utility.Formatters
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, enabled: Boolean = true, optional: Boolean = false) {
    var open by rememberSaveable { mutableStateOf(false) }
    val date = remember(value) { runCatching { LocalDate.parse(value) }.getOrNull() }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = date?.let(Formatters::date).orEmpty(), onValueChange = {}, readOnly = true,
            label = { Text(label) }, placeholder = { Text("Pilih tanggal") }, enabled = enabled,
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
            trailingIcon = { IconButton(onClick = { open = true }, enabled = enabled) { Icon(Icons.Rounded.CalendarToday, "Pilih $label") } })
        if (optional && value.isNotBlank()) OutlinedButton(onClick = { onValueChange("") }, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Kosongkan tanggal") }
    }
    if (open) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = (date ?: LocalDate.now()).toEpochDay() * 86_400_000L)
        DatePickerDialog(onDismissRequest = { open = false },
            confirmButton = { TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { onValueChange(LocalDate.ofEpochDay(it / 86_400_000L).toString()) }
                open = false
            }) { Text("Pilih tanggal") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Batal") } }) { DatePicker(state = picker) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionField(value: String, options: List<String>, label: String, enabled: Boolean = true, onSelected: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { if (enabled) open = it }) {
        OutlinedTextField(value, {}, readOnly = true, label = { Text(label) }, enabled = enabled,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) })
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); open = false }) }
        }
    }
}
