package com.minimechanicserviceapp.presentation.details

import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.model.OpenStatus
import java.time.DayOfWeek
import java.time.LocalTime

data class DaySchedule(
    val day: DayOfWeek,
    val opensAt: LocalTime?,
    val closesAt: LocalTime?,
    val isToday: Boolean,
) {
    val isClosedAllDay: Boolean get() = opensAt == null || closesAt == null
}

data class MechanicDetails(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val locality: String,
    val phoneNumber: String,
    val services: List<String>,
    val distanceKm: Double,
    val openStatus: OpenStatus,
    val week: List<DaySchedule>,
)

data class DetailsUiState(
    val isLoading: Boolean = true,
    val details: MechanicDetails? = null,
    val error: DataError? = null,
    val notFound: Boolean = false,
)