package com.vehiclemaintenancepro.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AppActionRow(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val stack = maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp), maxItemsInEachRow = if (stack) 1 else 2) {
            content(Modifier.weight(1f).heightIn(min = 48.dp))
        }
    }
}
