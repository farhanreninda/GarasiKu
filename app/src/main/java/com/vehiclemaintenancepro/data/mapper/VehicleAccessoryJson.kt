package com.vehiclemaintenancepro.data.mapper

import com.vehiclemaintenancepro.domain.model.AccessoryStatus
import com.vehiclemaintenancepro.domain.model.VehicleAccessory
import org.json.JSONArray
import org.json.JSONObject

object VehicleAccessoryJson {
    fun encode(items: List<VehicleAccessory>): String = JSONArray().apply {
        items.forEach { item ->
            put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                item.price?.let { put("price", it) }
                put("status", item.status.name)
            })
        }
    }.toString()

    fun decode(value: String): List<VehicleAccessory> {
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            VehicleAccessory(item.getString("id"), item.getString("name"),
                if (item.has("price") && !item.isNull("price")) item.getLong("price") else null,
                AccessoryStatus.valueOf(item.getString("status")))
        }
    }
}
