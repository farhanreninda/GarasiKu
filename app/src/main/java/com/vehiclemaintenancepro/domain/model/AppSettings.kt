package com.vehiclemaintenancepro.domain.model

data class AppSettings(
    val userName: String = "",
    val themeMode: ThemeMode = ThemeMode.System,
    val notificationsEnabled: Boolean = true,
    val taxReminderDays: Int = 7,
    val serviceReminderDays: Int = 7,
) {
    init { require(taxReminderDays in 0..365 && serviceReminderDays in 0..365) }
    fun reminderDays(type: ReminderType): Long = when (type) {
        ReminderType.Tax, ReminderType.Registration -> taxReminderDays.toLong()
        ReminderType.Service, ReminderType.OilChange -> serviceReminderDays.toLong()
        else -> 7L
    }
}
