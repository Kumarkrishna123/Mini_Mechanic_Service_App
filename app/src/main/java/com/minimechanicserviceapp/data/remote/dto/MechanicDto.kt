package com.minimechanicserviceapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MechanicDto(
    val id: String,
    val name: String? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val address: String? = null,
    val locality: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phoneNumber: String? = null,
    val services: List<String>? = null,
    val workingHours: List<WorkingHoursDto>? = null,
)

@Serializable
data class WorkingHoursDto(
    val day: String? = null,
    val opensAt: String? = null,
    val closesAt: String? = null,
)