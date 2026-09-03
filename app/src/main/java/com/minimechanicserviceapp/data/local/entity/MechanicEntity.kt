package com.minimechanicserviceapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "mechanics")
data class MechanicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val locality: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val services: List<String>,
    val workingHours: List<CachedWorkingHours>,
)

@Serializable
data class CachedWorkingHours(
    val day: String,
    val opensAt: String,
    val closesAt: String,
)