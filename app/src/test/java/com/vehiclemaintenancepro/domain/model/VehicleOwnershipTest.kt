package com.vehiclemaintenancepro.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class VehicleOwnershipTest {
    @Test fun rejectsNegativePricesAndDeliveryBeforeOrder() {
        val request = VehicleDetailsUpdateRequest(null, null, 32_075_000, 31_775_000, "Daya Motor", LocalDate.of(2023, 5, 27), LocalDate.of(2023, 7, 13))
        request.validate()
        assertThrows(IllegalArgumentException::class.java) { request.copy(purchasePrice = -1).validate() }
        assertThrows(IllegalArgumentException::class.java) { request.copy(otrPrice = -1).validate() }
        assertThrows(IllegalArgumentException::class.java) { request.copy(deliveryDate = LocalDate.of(2023, 5, 26)).validate() }
        request.copy(otrPrice = null, purchasePrice = null, orderDate = null, deliveryDate = null).validate()
    }
}
