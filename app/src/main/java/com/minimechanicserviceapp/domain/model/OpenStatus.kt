package com.minimechanicserviceapp.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

sealed interface OpenStatus {

    data class Open(val closesAt : LocalTime) : OpenStatus

    data class Closed(
        val opensAt : LocalTime?,
        val opensOn : DayOfWeek?,
    ) : OpenStatus

    val isOpen  : Boolean get() = this is Open

}
