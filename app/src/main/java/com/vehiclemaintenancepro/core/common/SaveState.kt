package com.vehiclemaintenancepro.core.common

data class SaveState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
