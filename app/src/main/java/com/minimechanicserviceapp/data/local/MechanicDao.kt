package com.minimechanicserviceapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.minimechanicserviceapp.data.local.entity.MechanicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MechanicDao {

    @Query("SELECT * FROM mechanics")
    fun observeAll(): Flow<List<MechanicEntity>>

    @Query("SELECT * FROM mechanics WHERE id = :id")
    fun observeById(id: String): Flow<MechanicEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(mechanics: List<MechanicEntity>)

    @Query("DELETE FROM mechanics")
    suspend fun clear()
    @Transaction
    suspend fun replaceAll(mechanics: List<MechanicEntity>) {
        clear()
        upsertAll(mechanics)
      }
}