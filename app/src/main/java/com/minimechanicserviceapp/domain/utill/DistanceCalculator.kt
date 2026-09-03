package com.minimechanicserviceapp.domain.utill

import kotlin.math.*

object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    fun distanceKm(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): Double {
        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(startLat)) *
                cos(Math.toRadians(endLat)) *
                sin(dLng / 2).pow(2)

        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}