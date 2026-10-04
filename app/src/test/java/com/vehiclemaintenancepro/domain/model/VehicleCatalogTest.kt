package com.vehiclemaintenancepro.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleCatalogTest {
    @Test
    fun `pcx uses cvt gasoline spec`() {
        val spec = checkNotNull(VehicleCatalog.specFor(
            vehicleType = VehicleType.Motorcycle,
            brand = "Honda",
            model = "PCX",
        ))

        assertEquals(TransmissionType.Cvt, spec.transmissionType)
        assertEquals(FuelType.Gasoline, spec.fuelType)
    }

    @Test
    fun `electric car uses electric drive spec`() {
        val spec = checkNotNull(VehicleCatalog.specFor(
            vehicleType = VehicleType.Car,
            brand = "Wuling",
            model = "Air EV",
        ))

        assertEquals(TransmissionType.ElectricDrive, spec.transmissionType)
        assertEquals(FuelType.Electric, spec.fuelType)
    }

    @Test
    fun `blank model has no automatic spec`() {
        assertNull(
            VehicleCatalog.specFor(
                vehicleType = VehicleType.Car,
                brand = "Toyota",
                model = "",
            ),
        )
    }

    @Test
    fun `gasoline car variants are not guessed`() {
        assertNull(
            VehicleCatalog.specFor(
                vehicleType = VehicleType.Car,
                brand = "Toyota",
                model = "Raize",
            ),
        )
    }
}
