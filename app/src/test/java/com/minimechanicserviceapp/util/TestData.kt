package com.minimechanicserviceapp.util

import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.model.WorkingHours
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Hours that make [com.minimechanicserviceapp.domain.utill.OpenStatusResolver] report
 * Open no matter when the suite runs, so open/closed assertions stay deterministic.
 */
val alwaysOpen: List<WorkingHours> = DayOfWeek.entries.map { day ->
    WorkingHours(day = day, opensAt = LocalTime.MIN, closesAt = LocalTime.MAX)
}

fun mechanic(
    id: String,
    name: String,
    rating: Double = 4.0,
    reviewCount: Int = 10,
    locality: String = "Indiranagar",
    address: String = "12 MG Road, Bengaluru",
    latitude: Double = 12.9719,
    longitude: Double = 77.6412,
    phoneNumber: String = "+919876543210",
    services: List<String> = listOf("Oil Change"),
    workingHours: List<WorkingHours> = alwaysOpen,
) = Mechanic(
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
    workingHours = workingHours,
)

/** Roughly 5 km from [com.minimechanicserviceapp.domain.utill.UserLocation]. */
const val NEAR_LAT = 12.9719
const val NEAR_LNG = 77.6412

/** Roughly 8 km away. */
const val MID_LAT = 12.9000
const val MID_LNG = 77.5946

/** Roughly 14 km away. */
const val FAR_LAT = 13.1007
const val FAR_LNG = 77.5963
