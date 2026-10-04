package com.vehiclemaintenancepro.data.mapper

import com.vehiclemaintenancepro.domain.model.MaintenanceAction
import com.vehiclemaintenancepro.domain.model.MaintenanceComponent
import com.vehiclemaintenancepro.domain.model.MaintenanceWorkItem
import org.json.JSONArray
import org.json.JSONObject

object MaintenanceWorkItemJson {
    fun encode(items: List<MaintenanceWorkItem>): String = JSONArray().apply {
        items.forEach { item ->
            put(JSONObject().apply {
                put("component", item.component.name)
                put("action", item.action.name)
                put("description", item.description)
                item.intervalKm?.let { put("intervalKm", it) }
                item.intervalMonths?.let { put("intervalMonths", it) }
            })
        }
    }.toString()

    fun decode(value: String): List<MaintenanceWorkItem> {
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            MaintenanceWorkItem(
                component = MaintenanceComponent.valueOf(item.getString("component")),
                action = MaintenanceAction.valueOf(item.getString("action")),
                description = item.optString("description"),
                intervalKm = if (item.has("intervalKm")) item.getLong("intervalKm") else null,
                intervalMonths = if (item.has("intervalMonths")) item.getInt("intervalMonths") else null,
            )
        }
    }
}
