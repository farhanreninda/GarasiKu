package com.vehiclemaintenancepro.presentation.component

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> FilterOptions(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f) 2 else 3
            FlowRow(Modifier.fillMaxWidth().selectableGroup().padding(4.dp),
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                values.forEach { value ->
                    val active = value == selected
                    Surface(modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
                        Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(active, enabled = enabled, role = Role.RadioButton,
                            onClick = { onSelected(value) }).padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center) {
                            Text(label(value), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
