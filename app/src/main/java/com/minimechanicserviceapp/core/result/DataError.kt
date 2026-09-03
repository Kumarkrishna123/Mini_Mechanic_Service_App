package com.minimechanicserviceapp.core.result



sealed interface DataError {

    data object NoInternet : DataError
    data object TimeOut  : DataError
    data class Server(val code : Int) : DataError
    data object Serialization : DataError
    data object Unknown : DataError

}