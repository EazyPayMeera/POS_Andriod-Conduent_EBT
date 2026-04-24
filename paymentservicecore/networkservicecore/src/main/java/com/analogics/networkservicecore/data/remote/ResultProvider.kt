package com.analogics.networkservicecore.data.remote

/**
 * Generic sealed class used to represent API/network operation states.
 *
 * This helps standardize response handling across:
 * - Success state
 * - Error state
 * - Loading state
 */
sealed class ResultProvider<out T> {

    /**
     * Represents successful result with parsed data.
     */
    data class Success<out T>(val data: T) : ResultProvider<T>()

    /**
     * Represents failure with exception details.
     */
    data class Error(val exception: Exception) : ResultProvider<Nothing>()

    /**
     * Represents loading state (useful for async operations / UI state).
     */
    object Loading : ResultProvider<Nothing>()
}


sealed class ResultProviderString<out T> {
    data class Success<out T>(val data: String) : ResultProvider<T>()
    //   data class Error<out T>(val data: T) : ResultProvider<T>()
    data class Error(val exception: Exception) : ResultProvider<Nothing>()
    object Loading : ResultProvider<Nothing>()
}