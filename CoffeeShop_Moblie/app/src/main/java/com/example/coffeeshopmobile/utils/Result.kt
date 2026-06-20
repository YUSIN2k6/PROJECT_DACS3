package com.example.coffeeshopmobile.utils

/**
 * Sealed class Result - Wrapper class cho async operations
 * Giúp ViewModel nhận biết chính xác trạng thái từ Repository
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}