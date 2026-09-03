package com.minimechanicserviceapp.domain.model

data class Mechanic(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val locality: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val services: List<String>,
    val workingHours: List<WorkingHours>,
)
