package com.minimechanicserviceapp.core.result

import kotlinx.serialization.SerializationException
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException

fun Throwable.toDataError() : DataError = when(this){
    is SocketTimeoutException -> DataError.TimeOut
    is IOException            -> DataError.NoInternet
    is HttpException          -> DataError.Server(code())
    is SerializationException  -> DataError.Serialization
    else -> DataError.Unknown
}