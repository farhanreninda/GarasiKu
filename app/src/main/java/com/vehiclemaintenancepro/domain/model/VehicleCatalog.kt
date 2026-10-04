package com.vehiclemaintenancepro.domain.model

object VehicleCatalog {
    val cars = listOf(
        VehicleBrand(
            name = "Toyota",
            models = listOf("Avanza", "Veloz", "Innova", "Fortuner", "Raize", "Yaris", "Rush", "Agya", "Calya", "Hilux", "Camry", "Corolla Cross"),
        ),
        VehicleBrand(
            name = "Honda",
            models = listOf("Brio", "Jazz", "City", "Civic", "Accord", "BR-V", "HR-V", "CR-V", "WR-V", "Mobilio"),
        ),
        VehicleBrand(
            name = "Daihatsu",
            models = listOf("Xenia", "Terios", "Ayla", "Sigra", "Rocky", "Gran Max", "Sirion", "Luxio"),
        ),
        VehicleBrand(
            name = "Suzuki",
            models = listOf("Ertiga", "XL7", "Carry", "Ignis", "Baleno", "S-Presso", "Jimny", "Grand Vitara"),
        ),
        VehicleBrand(
            name = "Mitsubishi",
            models = listOf("Xpander", "Xpander Cross", "Pajero Sport", "Triton", "Outlander PHEV", "L300"),
        ),
        VehicleBrand(
            name = "Hyundai",
            models = listOf("Creta", "Stargazer", "Palisade", "Santa Fe", "Ioniq 5", "Ioniq 6", "Kona Electric"),
        ),
        VehicleBrand(
            name = "Wuling",
            models = listOf("Air EV", "Binguo EV", "Cloud EV", "Alvez", "Confero", "Cortez", "Almaz"),
        ),
        VehicleBrand(
            name = "BMW",
            models = listOf("3 Series", "5 Series", "X1", "X3", "X5", "M3", "iX"),
        ),
        VehicleBrand(
            name = "Mercedes-Benz",
            models = listOf("A-Class", "C-Class", "E-Class", "GLA", "GLC", "GLE", "S-Class"),
        ),
    )

    val motorcycles = listOf(
        VehicleBrand(
            name = "Honda",
            models = listOf("BeAT", "Vario 125", "Vario 160", "PCX", "ADV 160", "Scoopy", "Genio", "CB150R", "CBR150R", "CRF150L", "Revo", "Supra X"),
        ),
        VehicleBrand(
            name = "Yamaha",
            models = listOf("Mio", "Fazzio", "Gear 125", "FreeGo", "Aerox", "NMAX", "XMAX", "Lexi", "Vixion", "R15", "MT-15", "WR155 R"),
        ),
        VehicleBrand(
            name = "Suzuki",
            models = listOf("Nex II", "Address", "Satria F150", "GSX-R150", "GSX-S150", "V-Strom 250SX", "Burgman Street"),
        ),
        VehicleBrand(
            name = "Kawasaki",
            models = listOf("Ninja 250", "Ninja ZX-25R", "W175", "KLX150", "D-Tracker", "Versys-X 250", "Z250"),
        ),
        VehicleBrand(
            name = "Vespa",
            models = listOf("LX", "S", "Sprint", "Primavera", "GTS", "946"),
        ),
        VehicleBrand(
            name = "Royal Enfield",
            models = listOf("Classic 350", "Meteor 350", "Hunter 350", "Himalayan", "Interceptor 650", "Continental GT"),
        ),
        VehicleBrand(
            name = "Harley-Davidson",
            models = listOf("Street Bob", "Fat Boy", "Iron 883", "Sportster S", "Pan America", "Road Glide"),
        ),
        VehicleBrand(
            name = "Triumph",
            models = listOf("Bonneville", "Speed 400", "Scrambler 400 X", "Trident 660", "Tiger Sport 660", "Street Triple"),
        ),
    )

    fun brandsFor(type: VehicleType): List<VehicleBrand> = when (type) {
        VehicleType.Car -> cars
        VehicleType.Motorcycle -> motorcycles
    }

    fun specFor(
        vehicleType: VehicleType,
        brand: String,
        model: String,
    ): VehicleModelSpec? {
        val brandName = brand.trim()
        val modelName = model.trim()
        if (brandName.isBlank() || modelName.isBlank()) return null

        return explicitSpecs[specKey(vehicleType, brandName, modelName)] ?: inferSpec(vehicleType, modelName)
    }

    private fun inferSpec(
        vehicleType: VehicleType,
        model: String,
    ): VehicleModelSpec? = when (vehicleType) {
        VehicleType.Car -> inferCarSpec(model)
        VehicleType.Motorcycle -> inferMotorcycleSpec(model)
    }

    private fun inferCarSpec(model: String): VehicleModelSpec? {
        val normalizedModel = model.normalized()
        return when {
            electricCars.any { it in normalizedModel } -> VehicleModelSpec(
                transmissionType = TransmissionType.ElectricDrive,
                fuelType = FuelType.Electric,
            )
            hybridCars.any { it in normalizedModel } -> VehicleModelSpec(
                transmissionType = TransmissionType.Automatic,
                fuelType = FuelType.Hybrid,
            )
            else -> null
        }
    }

    private fun inferMotorcycleSpec(model: String): VehicleModelSpec {
        val normalizedModel = model.normalized()
        val transmissionType = if (manualMotorcycles.any { it in normalizedModel }) {
            TransmissionType.Manual
        } else {
            TransmissionType.Cvt
        }
        return VehicleModelSpec(
            transmissionType = transmissionType,
            fuelType = FuelType.Gasoline,
        )
    }

    private fun specKey(
        vehicleType: VehicleType,
        brand: String,
        model: String,
    ): String = "${vehicleType.name}:${brand.normalized()}:${model.normalized()}"

    private fun String.normalized(): String = trim().lowercase()

    private val explicitSpecs = mapOf(
        specKey(VehicleType.Motorcycle, "Honda", "PCX") to VehicleModelSpec(
            transmissionType = TransmissionType.Cvt,
            fuelType = FuelType.Gasoline,
        ),
        specKey(VehicleType.Motorcycle, "Yamaha", "NMAX") to VehicleModelSpec(
            transmissionType = TransmissionType.Cvt,
            fuelType = FuelType.Gasoline,
        ),
        specKey(VehicleType.Motorcycle, "Yamaha", "Aerox") to VehicleModelSpec(
            transmissionType = TransmissionType.Cvt,
            fuelType = FuelType.Gasoline,
        ),
        specKey(VehicleType.Car, "Wuling", "Air EV") to VehicleModelSpec(
            transmissionType = TransmissionType.ElectricDrive,
            fuelType = FuelType.Electric,
        ),
        specKey(VehicleType.Car, "Wuling", "Binguo EV") to VehicleModelSpec(
            transmissionType = TransmissionType.ElectricDrive,
            fuelType = FuelType.Electric,
        ),
        specKey(VehicleType.Car, "Wuling", "Cloud EV") to VehicleModelSpec(
            transmissionType = TransmissionType.ElectricDrive,
            fuelType = FuelType.Electric,
        ),
        specKey(VehicleType.Car, "Hyundai", "Ioniq 5") to VehicleModelSpec(
            transmissionType = TransmissionType.ElectricDrive,
            fuelType = FuelType.Electric,
        ),
        specKey(VehicleType.Car, "Hyundai", "Ioniq 6") to VehicleModelSpec(
            transmissionType = TransmissionType.ElectricDrive,
            fuelType = FuelType.Electric,
        ),
    )

    private val electricCars = setOf("ev", "electric", "ioniq", "ix")
    private val hybridCars = setOf("hybrid", "phev", "grand vitara", "corolla cross")
    private val manualMotorcycles = setOf(
        "cb150r",
        "cbr150r",
        "crf150l",
        "revo",
        "supra",
        "vixion",
        "r15",
        "mt-15",
        "wr155",
        "satria",
        "gsx",
        "v-strom",
        "ninja",
        "w175",
        "klx",
        "d-tracker",
        "versys",
        "z250",
        "classic",
        "meteor",
        "hunter",
        "himalayan",
        "interceptor",
        "continental",
        "street bob",
        "fat boy",
        "iron",
        "sportster",
        "pan america",
        "road glide",
        "bonneville",
        "speed 400",
        "scrambler",
        "trident",
        "tiger",
        "street triple",
    )
}

data class VehicleBrand(
    val name: String,
    val models: List<String>,
)

data class VehicleModelSpec(
    val transmissionType: TransmissionType,
    val fuelType: FuelType,
)
