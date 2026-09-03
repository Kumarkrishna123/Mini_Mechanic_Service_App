package com.minimechanicserviceapp.data.local

import androidx.room.TypeConverter
import com.minimechanicserviceapp.data.local.entity.CachedWorkingHours
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun fromWorkingHours(value: List<CachedWorkingHours>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toWorkingHours(value: String): List<CachedWorkingHours> =
        json.decodeFromString(value)
}