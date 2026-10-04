package com.vehiclemaintenancepro.core.utility

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Formatters {
    private val indonesianLocale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()

    fun currency(amount: Long): String = NumberFormat
        .getCurrencyInstance(indonesianLocale)
        .format(amount)
        .replace(",00", "")

    fun fuelEfficiency(value: Double): String = String.format(indonesianLocale, "%.1f km/l", value)

    fun odometer(value: Long): String = String.format(indonesianLocale, "%,d km", value)

    fun date(date: LocalDate): String = date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(indonesianLocale),
    )
}
