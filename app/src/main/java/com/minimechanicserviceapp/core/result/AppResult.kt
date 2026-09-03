package com.minimechanicserviceapp.core.result

sealed interface AppResult<out T> {

    data class Success<out T>(val data : T ) : AppResult<T>
    data class Failure(val error : DataError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit) : AppResult<T>{
    if(this is  AppResult.Success ) action(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (DataError) -> Unit) : AppResult<T>{
    if(this is  AppResult.Failure ) action(error)
    return this
}