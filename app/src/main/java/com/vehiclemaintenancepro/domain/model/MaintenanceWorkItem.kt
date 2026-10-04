package com.vehiclemaintenancepro.domain.model

enum class ActivityCategory(val label: String) {
    Service("Servis"), OilChange("Ganti oli"), Repair("Perbaikan"), Accessory("Aksesori"), Other("Lainnya"),
}

enum class MaintenanceComponent(val label: String) {
    RoutineService("Servis berkala"), EngineOil("Oli mesin"), FinalDriveOil("Oli gardan"),
    Cvt("CVT"), DriveBelt("V-belt"), SparkPlug("Busi"), AirFilter("Filter udara"),
    Coolant("Coolant"), BrakeFluid("Minyak rem"), Injection("Injeksi"),
    Suspension("Suspensi"), Steering("Kemudi"), Accessory("Aksesori"), Other("Lainnya"),
}

enum class MaintenanceAction(val label: String) {
    Replace("Ganti"), Service("Servis / bersihkan"), Inspect("Periksa"), Repair("Perbaiki"), Install("Pasang"),
}

data class MaintenanceWorkItem(
    val component: MaintenanceComponent,
    val action: MaintenanceAction,
    val description: String = "",
    val intervalKm: Long? = null,
    val intervalMonths: Int? = null,
)
