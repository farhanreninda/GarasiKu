package com.vehiclemaintenancepro.core.common

sealed interface ResultState<out T> {
    data object Loading : ResultState<Nothing>
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : ResultState<Nothing>
}
