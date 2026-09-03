package com.minimechanicserviceapp.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class WorkingHours (
    val day : DayOfWeek,
    val opensAt : LocalTime,
    val closesAt : LocalTime
)