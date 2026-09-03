package com.minimechanicserviceapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minimechanicserviceapp.data.local.entity.MechanicEntity

@Database(
    entities = [MechanicEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mechanicDao(): MechanicDao
}