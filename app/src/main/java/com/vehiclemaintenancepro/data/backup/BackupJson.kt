package com.vehiclemaintenancepro.data.backup

import com.vehiclemaintenancepro.data.local.entity.*
import com.vehiclemaintenancepro.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class DataBackup(
    val exportedAtMillis: Long,
    val settings: AppSettings,
    val vehicles: List<VehicleEntity>,
    val activities: List<ActivityLogEntity>,
    val reminders: List<MaintenanceReminderEntity>,
) {
    val accessoryCount: Int get() = vehicles.sumOf { JSONArray(it.accessoriesJson).length() }
}

object BackupJson {
    const val MAX_BYTES = 10 * 1024 * 1024
    fun encode(data: DataBackup): String = JSONObject().apply {
        put("format", "vehicle-maintenance-pro")
        put("version", 1)
        put("exportedAtMillis", data.exportedAtMillis)
        put("settings", JSONObject().put("userName", data.settings.userName).put("themeMode", data.settings.themeMode.name)
            .put("notificationsEnabled", data.settings.notificationsEnabled)
            .put("taxReminderDays", data.settings.taxReminderDays).put("serviceReminderDays", data.settings.serviceReminderDays))
        put("vehicles", JSONArray().apply { data.vehicles.forEach { put(encodeVehicle(it)) } })
        put("activities", JSONArray().apply { data.activities.forEach { put(encodeActivity(it)) } })
        put("reminders", JSONArray().apply { data.reminders.forEach { put(encodeReminder(it)) } })
    }.toString(2)

    fun decode(text: String): DataBackup {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "File melebihi batas 10 MB." }
        try {
            val parser = org.json.JSONTokener(text)
            val root = parser.nextValue() as? JSONObject ?: error("Isi cadangan harus berupa objek JSON")
            require(parser.nextClean() == '\u0000') { "Ada isi tambahan setelah JSON" }
            require(root.string("format") == "vehicle-maintenance-pro" && root.number("version") == 1L) {
                "Format atau versi cadangan tidak didukung."
            }
            val settings = root.getJSONObject("settings")
            return DataBackup(root.number("exportedAtMillis"),
                AppSettings(settings.string("userName"), ThemeMode.valueOf(settings.string("themeMode")),
                    if (settings.has("notificationsEnabled")) settings.boolean("notificationsEnabled") else true,
                    if (settings.has("taxReminderDays")) settings.integer("taxReminderDays") else 7,
                    if (settings.has("serviceReminderDays")) settings.integer("serviceReminderDays") else 7),
                root.getJSONArray("vehicles").rows(::decodeVehicle),
                root.getJSONArray("activities").rows(::decodeActivity),
                root.getJSONArray("reminders").rows(::decodeReminder),
            ).also(::validate)
        } catch (error: Exception) {
            throw IllegalArgumentException("Cadangan tidak valid: ${error.message ?: "periksa file JSON"}", error)
        }
    }

    private fun <T> JSONArray.rows(read: (JSONObject) -> T): List<T> = (0 until length()).map { read(getJSONObject(it)) }
    private fun JSONObject.string(key: String): String = (get(key) as? String) ?: error("$key harus berupa teks")
    private fun JSONObject.optionalString(key: String): String? = if (isNull(key)) null else string(key)
    private fun JSONObject.number(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long) { "$key harus berupa bilangan bulat" }
        return (value as Number).toLong()
    }
    private fun JSONObject.optionalNumber(key: String): Long? = if (isNull(key)) null else number(key)
    private fun JSONObject.integer(key: String): Int = number(key).also { require(it in Int.MIN_VALUE..Int.MAX_VALUE) }.toInt()
    private fun JSONObject.optionalInteger(key: String): Int? = if (isNull(key)) null else integer(key)
    private fun JSONObject.boolean(key: String): Boolean = (get(key) as? Boolean) ?: error("$key harus berupa boolean")

    private fun encodeVehicle(row: VehicleEntity): JSONObject = JSONObject().apply {
        put("id", row.id)
        put("vehicleType", row.vehicleType)
        put("photoUri", row.photoUri ?: JSONObject.NULL)
        put("brand", row.brand)
        put("model", row.model)
        put("year", row.year ?: JSONObject.NULL)
        put("licensePlate", row.licensePlate)
        put("color", row.color ?: JSONObject.NULL)
        put("engineNumber", row.engineNumber ?: JSONObject.NULL)
        put("frameNumber", row.frameNumber ?: JSONObject.NULL)
        put("transmissionType", row.transmissionType)
        put("fuelType", row.fuelType)
        put("odometerKm", row.odometerKm)
        put("purchasePrice", row.purchasePrice ?: JSONObject.NULL)
        put("purchaseDateEpochDay", row.purchaseDateEpochDay ?: JSONObject.NULL)
        put("note", row.note ?: JSONObject.NULL)
        put("isActive", row.isActive)
        put("isArchived", row.isArchived)
        put("createdAtMillis", row.createdAtMillis)
        put("updatedAtMillis", row.updatedAtMillis)
        put("otrPrice", row.otrPrice ?: JSONObject.NULL)
        put("dealer", row.dealer ?: JSONObject.NULL)
        put("orderDateEpochDay", row.orderDateEpochDay ?: JSONObject.NULL)
        put("accessoriesJson", JSONArray(row.accessoriesJson))
    }
    private fun decodeVehicle(row: JSONObject): VehicleEntity = VehicleEntity(
        id = row.number("id"),
        vehicleType = row.string("vehicleType"),
        photoUri = row.optionalString("photoUri"),
        brand = row.string("brand"),
        model = row.string("model"),
        year = row.optionalInteger("year"),
        licensePlate = row.string("licensePlate"),
        color = row.optionalString("color"),
        engineNumber = row.optionalString("engineNumber"),
        frameNumber = row.optionalString("frameNumber"),
        transmissionType = row.string("transmissionType"),
        fuelType = row.string("fuelType"),
        odometerKm = row.number("odometerKm"),
        purchasePrice = row.optionalNumber("purchasePrice"),
        purchaseDateEpochDay = row.optionalNumber("purchaseDateEpochDay"),
        note = row.optionalString("note"),
        isActive = row.boolean("isActive"),
        isArchived = row.boolean("isArchived"),
        createdAtMillis = row.number("createdAtMillis"),
        updatedAtMillis = row.number("updatedAtMillis"),
        otrPrice = row.optionalNumber("otrPrice"),
        dealer = row.optionalString("dealer"),
        orderDateEpochDay = row.optionalNumber("orderDateEpochDay"),
        accessoriesJson = row.getJSONArray("accessoriesJson").toString(),
    )

    private fun encodeActivity(row: ActivityLogEntity): JSONObject = JSONObject().apply {
        put("id", row.id)
        put("vehicleId", row.vehicleId ?: JSONObject.NULL)
        put("title", row.title)
        put("description", row.description ?: JSONObject.NULL)
        put("costAmount", row.costAmount ?: JSONObject.NULL)
        put("occurredAtMillis", row.occurredAtMillis)
        put("category", row.category ?: JSONObject.NULL)
        put("odometerKm", row.odometerKm ?: JSONObject.NULL)
        put("location", row.location ?: JSONObject.NULL)
        put("workItemsJson", JSONArray(row.workItemsJson))
    }
    private fun decodeActivity(row: JSONObject): ActivityLogEntity = ActivityLogEntity(
        id = row.number("id"),
        vehicleId = row.optionalNumber("vehicleId"),
        title = row.string("title"),
        description = row.optionalString("description"),
        costAmount = row.optionalNumber("costAmount"),
        occurredAtMillis = row.number("occurredAtMillis"),
        category = row.optionalString("category"),
        odometerKm = row.optionalNumber("odometerKm"),
        location = row.optionalString("location"),
        workItemsJson = row.getJSONArray("workItemsJson").toString(),
    )

    private fun encodeReminder(row: MaintenanceReminderEntity): JSONObject = JSONObject().apply {
        put("id", row.id)
        put("vehicleId", row.vehicleId)
        put("type", row.type)
        put("title", row.title)
        put("dueDateEpochDay", row.dueDateEpochDay ?: JSONObject.NULL)
        put("dueTimeSecondOfDay", row.dueTimeSecondOfDay ?: JSONObject.NULL)
        put("dueOdometerKm", row.dueOdometerKm ?: JSONObject.NULL)
        put("isCompleted", row.isCompleted)
        put("createdAtMillis", row.createdAtMillis)
        put("updatedAtMillis", row.updatedAtMillis)
    }
    private fun decodeReminder(row: JSONObject): MaintenanceReminderEntity = MaintenanceReminderEntity(
        id = row.number("id"),
        vehicleId = row.number("vehicleId"),
        type = row.string("type"),
        title = row.string("title"),
        dueDateEpochDay = row.optionalNumber("dueDateEpochDay"),
        dueTimeSecondOfDay = row.optionalInteger("dueTimeSecondOfDay"),
        dueOdometerKm = row.optionalNumber("dueOdometerKm"),
        isCompleted = row.boolean("isCompleted"),
        createdAtMillis = row.number("createdAtMillis"),
        updatedAtMillis = row.number("updatedAtMillis"),
    )

    private fun validate(data: DataBackup) {
        fun nonnegative(value: Long?) { require(value == null || value >= 0) { "Angka negatif tidak diperbolehkan" } }
        fun date(value: Long?) { value?.let { LocalDate.ofEpochDay(it) } }
        fun ids(values: List<Long>) { require(values.all { it > 0 && it < Long.MAX_VALUE } && values.distinct().size == values.size) { "ID tidak valid atau duplikat" } }
        ids(data.vehicles.map { it.id }); ids(data.activities.map { it.id }); ids(data.reminders.map { it.id })
        val vehicleIds = data.vehicles.map { it.id }.toSet()
        require(data.vehicles.map { it.licensePlate.trim().uppercase(java.util.Locale.ROOT) }.distinct().size == data.vehicles.size) { "Plat kendaraan duplikat" }
        require(data.vehicles.count { it.isActive } <= 1) { "Lebih dari satu kendaraan utama" }
        require(data.exportedAtMillis in 0..253402300799999L) { "Tanggal cadangan tidak valid" }
        data.vehicles.forEach { row ->
            VehicleType.valueOf(row.vehicleType); TransmissionType.valueOf(row.transmissionType); FuelType.valueOf(row.fuelType)
            require(row.brand.isNotBlank() && row.model.isNotBlank() && row.licensePlate.isNotBlank())
            require(!row.isActive || !row.isArchived)
            require(row.year == null || row.year in 1886..9999)
            nonnegative(row.odometerKm); nonnegative(row.purchasePrice); nonnegative(row.otrPrice)
            nonnegative(row.createdAtMillis); nonnegative(row.updatedAtMillis)
            date(row.purchaseDateEpochDay); date(row.orderDateEpochDay)
            require(row.orderDateEpochDay == null || row.purchaseDateEpochDay == null || row.purchaseDateEpochDay >= row.orderDateEpochDay)
            val accessories = JSONArray(row.accessoriesJson).rows { item ->
                require(item.string("id").isNotBlank() && item.string("name").isNotBlank())
                AccessoryStatus.valueOf(item.string("status"))
                nonnegative(item.optionalNumber("price"))
                item.string("id")
            }
            require(accessories.distinct().size == accessories.size) { "ID aksesori duplikat" }
        }
        data.activities.forEach { row ->
            require(row.vehicleId == null || row.vehicleId in vehicleIds) { "Kendaraan pada aktivitas tidak ditemukan" }
            require(row.title.isNotBlank()); row.category?.let { ActivityCategory.valueOf(it) }
            nonnegative(row.costAmount); nonnegative(row.odometerKm); nonnegative(row.occurredAtMillis)
            JSONArray(row.workItemsJson).rows { item ->
                MaintenanceComponent.valueOf(item.string("component")); MaintenanceAction.valueOf(item.string("action"))
                item.string("description")
                item.optionalNumber("intervalKm")?.let { require(it > 0) }
                item.optionalInteger("intervalMonths")?.let { require(it > 0) }
            }
        }
        data.reminders.forEach { row ->
            require(row.vehicleId in vehicleIds) { "Kendaraan pada pengingat tidak ditemukan" }
            ReminderType.valueOf(row.type); require(row.title.isNotBlank())
            date(row.dueDateEpochDay); nonnegative(row.dueOdometerKm)
            require(row.dueTimeSecondOfDay == null || row.dueTimeSecondOfDay in 0..86399)
            nonnegative(row.createdAtMillis); nonnegative(row.updatedAtMillis)
        }
    }
}
