package com.eazypaytech.networkservicecore.nComponent

sealed class ResultProvider<out T> {
    data class Success<out T>(val data: T) : ResultProvider<T>()
    //   data class Error<out T>(val data: T) : ResultProvider<T>()
    data class Error(val exception: Exception) : ResultProvider<Nothing>()
    object Loading : ResultProvider<Nothing>()
}


sealed class ResultProviderString<out T> {
    data class Success<out T>(val data: String) : ResultProvider<T>()
    //   data class Error<out T>(val data: T) : ResultProvider<T>()
    data class Error(val exception: Exception) : ResultProvider<Nothing>()
    object Loading : ResultProvider<Nothing>()
}