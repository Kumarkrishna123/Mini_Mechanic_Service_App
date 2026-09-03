package com.minimechanicserviceapp.data.mapper

import com.minimechanicserviceapp.data.local.entity.CachedWorkingHours
import com.minimechanicserviceapp.data.local.entity.MechanicEntity
import com.minimechanicserviceapp.data.remote.dto.MechanicDto
import com.minimechanicserviceapp.data.remote.dto.WorkingHoursDto
import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.model.WorkingHours
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeParseException


fun  MechanicDto.toEntityOrNull() : MechanicEntity? {
    val safeName = name?.takeIf { it.isNotBlank() } ?: return null
    val lat = latitude ?: return null
    val lng = longitude ?: return null

    return MechanicEntity(
        id = id,
        name = safeName,
        rating = rating?.coerceIn(0.0, 5.0) ?: 0.0,
        reviewCount = reviewCount ?: 0,
        address = address.orEmpty(),
        locality = locality.orEmpty(),
        latitude = lat,
        longitude = lng,
        phoneNumber = phoneNumber.orEmpty(),
        services = services?.filter { it.isNotBlank() }.orEmpty(),
        workingHours = workingHours?.mapNotNull { it.toCachedOrNull() }.orEmpty(),
    )
}

private fun WorkingHoursDto.toCachedOrNull(): CachedWorkingHours? {
    val d = day?.takeIf { it.isNotBlank() } ?: return null
    val open = opensAt?.takeIf { it.isNotBlank() } ?: return null
    val close = closesAt?.takeIf { it.isNotBlank() } ?: return null
    return CachedWorkingHours(day = d, opensAt = open, closesAt = close)
}


fun MechanicEntity.toDomain(): Mechanic = Mechanic(
    id = id,
    name = name,
    rating = rating,
    reviewCount = reviewCount,
    address = address,
    locality = locality,
    latitude = latitude,
    longitude = longitude,
    phoneNumber = phoneNumber,
    services = services,
    workingHours = workingHours.mapNotNull { it.toDomainOrNull() },
)
private fun CachedWorkingHours.toDomainOrNull(): WorkingHours? = try {
    WorkingHours(
        day = DayOfWeek.valueOf(day.uppercase()),
        opensAt = LocalTime.parse(opensAt),
        closesAt = LocalTime.parse(closesAt),
    )
} catch (e: IllegalArgumentException) {
    null
} catch (e: DateTimeParseException) {
    null
}

