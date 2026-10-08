package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate
import java.util.UUID

enum class AccessoryStatus(val label: String) {
    Unspecified("Status belum dicatat"), Owned("Dimiliki"), Installed("Terpasang"),
}

data class VehicleAccessory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Long? = null,
    val status: AccessoryStatus = AccessoryStatus.Unspecified,
)

data class VehicleDetailsUpdateRequest(
    val engineNumber: String?,
    val frameNumber: String?,
    val otrPrice: Long?,
    val purchasePrice: Long?,
    val dealer: String?,
    val orderDate: LocalDate?,
    val deliveryDate: LocalDate?,
) {
    fun validate() {
        require(otrPrice == null || otrPrice >= 0) { "Harga OTR tidak valid" }
        require(purchasePrice == null || purchasePrice >= 0) { "Harga pembelian tidak valid" }
        require(orderDate == null || deliveryDate == null || !deliveryDate.isBefore(orderDate)) {
            "Tanggal datang tidak boleh sebelum tanggal indent"
        }
    }
}
